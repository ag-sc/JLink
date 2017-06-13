package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import candidateretrieval.jaccard.JaccardRetrieval;
import candidateretrieval.levenshtein.LevenshteinRetrieval;
import candidateretrieval.lucene.LuceneRetrieval;
import corpus.CorpusLoader;
import corpus.DocumentCorpus;
import corpus.SampledInstance;
import dictionary.Concept;
import evaluation.EvaluationUtil;
import exceptions.UnkownTemplateRequestedException;
import learning.AdvancedLearner;
import learning.Model;
import learning.ObjectiveFunction;
import learning.Trainer;
import learning.Trainer.EpochCallback;
import learning.Trainer.InstanceCallback;
import learning.optimizer.SGD;
import learning.regularizer.L2;
import learning.scorer.DefaultScorer;
import learning.scorer.LinearScorer;
import learning.scorer.Scorer;
import learning.scorer.SoftplusScorer;
import main.comparisonobjects.EntityDisambComparisonObject;
import main.comparisonobjects.EntityRecComparisonObject;
import main.evaluation.PRF1Extended;
import main.setting.EEvaluationMode;
import main.setting.Setting;
import objective.ConceptObjectiveFunction;
import objective.OverlapRecTrueConceptObjectiveFunction;
import sampler.DiseaseDisambiguationExplorer;
import sampler.MultipleTokenBoundaryExplorer;
import sampling.DefaultSampler;
import sampling.Explorer;
import sampling.Initializer;
import sampling.samplingstrategies.AcceptStrategies;
import sampling.samplingstrategies.AcceptStrategy;
import sampling.samplingstrategies.SamplingStrategies;
import sampling.samplingstrategies.SamplingStrategy;
import sampling.stoppingcriterion.StoppingCriterion;
import stoppingcriterion.StopAtMaxModelScore;
import stoppingcriterion.StopAtMaxObjectiveScore;
import templates.AbstractTemplate;
import templates.EmptyTemplate;
import templates.TemplateFactory;
import variables.AbstractState;
import variables.EEntityType;
import variables.EntityAnnotation;
import variables.JLinkState;
import variables.LabeledJlinkDocument;

public class JLink {

	public static Logger log = LogManager.getFormatterLogger(JLink.class.getSimpleName());

	public static PrintStream results;

	public final static List<String> filterIDs = Arrays.asList("CDR_developset_corpus_16584858");

	private List<LabeledJlinkDocument> trainingDocuments;

	private List<LabeledJlinkDocument> testDocuments;

	public static Setting setting;

	final static AtomicLong timeForTrainOnEpoch = new AtomicLong(0);

	private static final int NUMBER_OF_SAMPLING_STEPS_TRAIN = 300;
	private static final int NUMBER_OF_SAMPLING_STEPS_TEST = 300;

	private static final double L2_REGULARIZATION = 0.001;
	private static final SamplingStrategy<JLinkState> SAMPLING_STRATEGY = SamplingStrategies.greedyModelStrategy();
	private static final AcceptStrategy<JLinkState> ACCEPTANCE_STRATEGY = AcceptStrategies.strictModelAccept();

	public static final EType type = EType.Disease;

	public JLink(Setting setting) {
		JLink.setting = setting;
		initResultLoggingFile();
		results.println("\n\n\nPersonal notes for following results: " + setting.personalNotes + ":\n\n\n");

	}

	public void startTrainAndTestProcedure() throws FileNotFoundException, ClassNotFoundException, IOException,
			UnkownTemplateRequestedException, Exception {
		File modelFile = new File(buildModelDir() + buildModelName());
		testDocuments = loadTestDocuments();

		if (!modelFile.exists()) {
			trainingDocuments = loadTrainingDocuments();
			trainModel(EEvaluationMode.TRAIN_TEST);
			log.error("Model already exists! Skipping training...");
		} else {
			testModel(modelFile, setting.epochs - 1);
		}
	}

	public void startTrainOnlyProcedure() throws FileNotFoundException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {

		File modelFile = new File(buildModelDir() + buildModelName());
		if (!modelFile.exists()) {
			trainingDocuments = loadTrainingDocuments();
			trainModel(EEvaluationMode.TRAIN);
		} else {
			log.error("Model already exists!");
		}
	}

	public void startTestOnlyProcedure() throws FileNotFoundException, ClassNotFoundException, IOException,
			UnkownTemplateRequestedException, Exception {
		testDocuments = loadTestDocuments();

		File modelFile = new File(buildModelDir() + buildModelName());

		testModel(modelFile, setting.epochs - 1);

	}

	private String buildModelNamePrefix() {
		final StringBuffer modelName = new StringBuffer();
		modelName.append(setting.getShortString());
		return modelName.toString();
	}

	/**
	 * Model name is the model name prefix followed by the epoch.
	 * 
	 * @return the model name containing the epoch.
	 */
	private String buildModelName() {
		final StringBuffer modelName = new StringBuffer();
		modelName.append(buildModelNamePrefix());
		modelName.append("_epoch_");
		modelName.append(setting.epochs - 1);
		return modelName.toString();
	}

	private String buildModelDir() {
		final StringBuffer modelDir = new StringBuffer();
		modelDir.append(setting.modelRootDirectory + "/" + type.name().toLowerCase());
		modelDir.append("/models/").append(setting.corpus.name().toLowerCase()).append("/")
				.append(setting.testDataset.fullName.toLowerCase()).append("/").append(setting.getShortString())
				.append("/");
		return modelDir.toString();
	}

	public void initResultLoggingFile() {
		try {

			final File resultLoggingFile = new File(buildResultFileNamePrefix() + ".tsv");

			boolean exists = resultLoggingFile.exists();

			results = new PrintStream(new FileOutputStream(resultLoggingFile, true));

			if (!exists) {
				printHeader();
			}
		} catch (IOException e) {
			log.warn("Could not initialize result logging: " + e.getMessage());
		}
	}

	private String buildResultFileNamePrefix() {
		String resultFileNamePrefix = setting.modelRootDirectory + "/" + type.name().toLowerCase() + "/"
				+ setting.corpus.name().toLowerCase() + "_on_" + setting.testDataset.fullName.toLowerCase()
				+ (setting.runInDebug ? "_debug_" : "_") + setting.getShortString();
		return resultFileNamePrefix;
	}

	private void printHeader() {
		log.info("Result file does not exist...");
		results.print("Version\t");
		results.print("Alpha\t");
		results.print("Epoch\t");
		results.print("Scorer\t");
		setting.candidateRetrievalSetting.setting.entrySet()
				.forEach(e -> results.print(e.getKey().getSimpleName() + "\t"));
		setting.templateSetting.setting.entrySet().forEach(e -> results.print(e.getKey().getSimpleName() + "\t"));
		results.print("UNKCounter\t");
		results.print("Rec-Micro F1\t");
		results.print("Rec-Micro Precision\t");
		results.print("Rec-Micro Recall\t");
		results.print("Rec-Macro F1\t");
		results.print("Rec-Macro Precision\t");
		results.print("Rec-Macro Recall\t");
		results.print("Rec-tp\t");
		results.print("Rec-fp\t");
		results.print("Rec-fn\t");
		results.print("Set-Micro F1\t");
		results.print("Set-Micro Precision\t");
		results.print("Set-Micro Recall\t");
		results.print("Set-Macro F1\t");
		results.print("Set-Macro Precision\t");
		results.print("Set-Macro Recall\t");
		results.print("Set-tp\t");
		results.print("Set-fp\t");
		results.print("Set-fn\t");
		results.print("train time\t");
		results.print("test time\n");
	}

	private List<LabeledJlinkDocument> loadTestDocuments() {
		List<LabeledJlinkDocument> testDocuments;

		DocumentCorpus testCorpus = null;

		if (Main.FAKE_TEST_DATA)
			testCorpus = Main.testCorpus();
		else {

			switch (setting.testDataset) {
			case DEVELOP:
				testCorpus = CorpusLoader.loadDevelopCorpus();
				break;
			case TRAIN:
				testCorpus = CorpusLoader.loadTrainCorpus();
				break;
			case TEST:
				testCorpus = CorpusLoader.loadTestCorpus();
				break;
			}

		}

		testDocuments = testCorpus.getDocuments();

		if (!Main.FAKE_TEST_DATA && Main.FILTER) {
			testDocuments = testDocuments.stream().filter(d -> filterIDs.contains(d.getName()))
					.collect(Collectors.toList());
		}

		return testDocuments;
	}

	private List<LabeledJlinkDocument> loadTrainingDocuments() {
		List<LabeledJlinkDocument> trainingDocuments;
		DocumentCorpus trainCorpus = CorpusLoader.loadTrainCorpus();

		Collections.sort(trainCorpus.getDocuments(), new Comparator<LabeledJlinkDocument>() {

			@Override
			public int compare(LabeledJlinkDocument o1, LabeledJlinkDocument o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		trainingDocuments = trainCorpus.getDocuments();

		/*
		 * Inserted filter for non-annotated sentences.
		 */
		trainingDocuments = trainingDocuments.stream().filter(d -> !d.getResult().getEntities().isEmpty())
				.collect(Collectors.toList());
		return trainingDocuments;
	}

	@SuppressWarnings("unchecked")
	private void trainModel(EEvaluationMode evaluationMode) throws IOException, FileNotFoundException,
			InstantiationException, IllegalAccessException, ClassNotFoundException {

		ObjectiveFunction objective = new OverlapRecTrueConceptObjectiveFunction();
		// ObjectiveFunction objective = new
		// ConceptObjectiveFunction();

		List<AbstractTemplate<LabeledJlinkDocument, JLinkState, ?>> templates = new ArrayList<>();

		for (Entry<Class<? extends AbstractTemplate<?, ?, ?>>, Boolean> abstractTemplate : setting.templateSetting.setting
				.entrySet()) {

			final boolean includeTemplate = abstractTemplate.getValue();

			if (includeTemplate) {
				templates.add((AbstractTemplate<LabeledJlinkDocument, JLinkState, ?>) Class
						.forName(abstractTemplate.getKey().getName()).newInstance());
			}

		}

		List<Explorer<JLinkState>> explorers = buildExplorers();

		Scorer scorer = getScorer();

		Model<LabeledJlinkDocument, JLinkState> model = new Model<>(scorer, templates);
		model.setMultiThreaded(!Main.DEBUG);
		Initializer<LabeledJlinkDocument, JLinkState> initializer = d -> new JLinkState(d);

		StoppingCriterion<JLinkState> objectiveOne = new StopAtMaxObjectiveScore(NUMBER_OF_SAMPLING_STEPS_TRAIN);

		DefaultSampler<LabeledJlinkDocument, JLinkState, JLinkState> sampler = new DefaultSampler<>(model, objective,
				explorers, objectiveOne);
		sampler.setTrainSamplingStrategy(SAMPLING_STRATEGY);
		sampler.setTrainAcceptStrategy(ACCEPTANCE_STRATEGY);

		AdvancedLearner<JLinkState> learner = new AdvancedLearner<>(model, new SGD(setting.alpha, 0, 0, false),
				new L2(L2_REGULARIZATION));

		Trainer trainer = new Trainer();
		trainer.addEpochCallback(new EpochCallback() {

			public void onStartEpoch(Trainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {
				timeForTrainOnEpoch.set(System.currentTimeMillis());
			};

			@Override
			public void onEndEpoch(Trainer caller, int epoch, int numberOfEpochs, int numberOfInstances) {

				final File modelFile = new File(buildModelDir() + buildModelNamePrefix() + "_epoch_" + epoch);
				try {
					model.saveModelToFile(modelFile);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (evaluationMode == EEvaluationMode.TRAIN_TEST) {
					try {
						testModel(modelFile, epoch);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

		});

		log.info("####################");
		log.info("Start training");
		trainer.train(sampler, initializer, learner, trainingDocuments, setting.epochs);
		log.info("###############");

	}

	private List<Explorer<JLinkState>> buildExplorers() {
		List<Explorer<JLinkState>> explorers = new ArrayList<>();
		explorers.add(new MultipleTokenBoundaryExplorer(setting.templateSetting.setting));
		explorers.add(new DiseaseDisambiguationExplorer(setting.candidateRetrievalSetting.setting));
		return explorers;
	}

	@SuppressWarnings("unchecked")
	private void testModel(final File modelFileForTest, int epoch) throws IOException, FileNotFoundException,
			ClassNotFoundException, UnkownTemplateRequestedException, Exception {
		results.print(setting.version + "\t");
		results.print(setting.alpha + "\t");
		results.print(epoch + "\t");
		results.print(setting.scorerType + "\t");
		results.print(setting.candidateRetrievalSetting.setting.get(LuceneRetrieval.class) ? "yes\t" : "no\t");
		results.print(setting.candidateRetrievalSetting.setting.get(JaccardRetrieval.class) ? "yes\t" : "no\t");
		results.print(setting.candidateRetrievalSetting.setting.get(LevenshteinRetrieval.class) ? "yes\t" : "no\t");
		long timeForTesting = System.currentTimeMillis();
		for (int j = 0; j < setting.templateSetting.getBinarySetting().length; j++) {
			results.print(setting.templateSetting.getBinarySetting()[j] ? "yes\t" : "no\t");
		}
		List<SampledInstance<LabeledJlinkDocument, JLinkState, JLinkState>> predictions;

		Initializer<LabeledJlinkDocument, JLinkState> initializer = d -> new JLinkState(d);

		ObjectiveFunction objective = new OverlapRecTrueConceptObjectiveFunction();

		List<Explorer<JLinkState>> explorers = buildExplorers();

		StoppingCriterion<JLinkState> stopAtMaxModelScore = new StopAtMaxModelScore(NUMBER_OF_SAMPLING_STEPS_TEST);

		Trainer trainer = new Trainer();

		Scorer scorer = getScorer();

		Model<LabeledJlinkDocument, JLinkState> model = new Model<>(scorer);
		model.setMultiThreaded(!Main.DEBUG);
		model.loadModelFromDir(modelFileForTest.getAbsoluteFile(),
				new TemplateFactory<LabeledJlinkDocument, JLinkState>() {

					@SuppressWarnings("unchecked")
					@Override
					public AbstractTemplate<LabeledJlinkDocument, JLinkState, ?> newInstance(String templateName)
							throws UnkownTemplateRequestedException, Exception {

						return (AbstractTemplate<LabeledJlinkDocument, JLinkState, ?>) Class
								.forName(EmptyTemplate.class.getPackage().getName() + "." + templateName).newInstance();
					}
				});

		DefaultSampler<LabeledJlinkDocument, JLinkState, JLinkState> sampler = new DefaultSampler<>(model, objective,
				explorers, stopAtMaxModelScore);
		sampler.setTestSamplingStrategy(SAMPLING_STRATEGY);
		sampler.setTestAcceptStrategy(ACCEPTANCE_STRATEGY);

		ConceptObjectiveFunction of = new ConceptObjectiveFunction();

		trainer.addInstanceCallback(new InstanceCallback() {

			@Override
			public <InstanceT, StateT extends AbstractState<InstanceT>> void onEndInstance(Trainer caller,
					InstanceT instance, int indexOfInstance, StateT finalState, int numberOfInstances, int epoch,
					int numberOfEpochs) {
				JLinkState state = (JLinkState) finalState;
				JLinkState gold = ((LabeledJlinkDocument) state.getDocument()).getResult();
				of.score(state, gold);

			}
		});

		long t = System.currentTimeMillis();

		predictions = trainer.test(sampler, initializer, testDocuments);

		log.info("time = " + (System.currentTimeMillis() - t));

		evaluateResult(predictions);

		results.print((double) (System.currentTimeMillis() - timeForTrainOnEpoch.get()) / 60000d);
		results.println("\t" + (double) (System.currentTimeMillis() - timeForTesting) / 60000d);
		try {
			writeResultsToPubTatorFile(predictions, new File(buildResultFileNamePrefix() + ".PubTator"));
		} catch (Exception e) {

		}

		for (SampledInstance<LabeledJlinkDocument, JLinkState, JLinkState> prediction : predictions) {
			JLinkState goldState = prediction.getInstance().getResult();
			objective.score(prediction.getState(), goldState);
		}
		log.info("Overall performance:");
		EvaluationUtil
				.printPredictionPerformance(predictions.stream().map(p -> p.getState()).collect(Collectors.toList()));

		// predictions = trainer.test(sampler, initializer, allTrain);
		//
		// evaluateResult(predictions);
		//
		// for (State state : predictions) {
		// State goldState = ((LabeledDocument<State, State>)
		// state.getDocument()).getGoldResult();
		// objective.score(state, goldState);
		// }
		// log.info("Overall performance:");
		// EvaluationUtil.printPredictionPerformance(predictions);

	}

	private Scorer getScorer() {
		Scorer scorer;

		switch (setting.scorerType) {
		case EXP:
			scorer = new DefaultScorer();
			break;
		case LINEAR:
			scorer = new LinearScorer();
			break;
		case SOFTPLUS:
			scorer = new SoftplusScorer();
			break;
		default:
			scorer = null;
		}
		return scorer;
	}

	private void writeResultsToPubTatorFile(
			List<SampledInstance<LabeledJlinkDocument, JLinkState, JLinkState>> predictions, final File outfile)
			throws FileNotFoundException {

		PrintStream resultFile = new PrintStream(outfile);

		for (SampledInstance<LabeledJlinkDocument, JLinkState, JLinkState> resultState : predictions) {

			final String docuemntID = resultState.getInstance().getName().split("-")[0].split("_")[3];

			for (EntityAnnotation resultEntity : resultState.getState().getEntities()) {
				if (resultEntity.getType().equals(new Concept("MESH:-1", EEntityType.UNK)))
					continue;

				if (resultEntity.getType().equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
					continue;

				resultFile.println(docuemntID + "\t" + resultEntity.getOriginalStart() + "\t"
						+ resultEntity.getOriginalEnd() + "\t" + resultEntity.getOriginalText() + "\t" + type.name()
						+ "\t" + resultEntity.getType().conceptID + "\t1");
				// resultFile.println(docuemntID + "\t" +
				// resultEntity.getBeginTokenIndex() + "\t"
				// + resultEntity.getEndTokenIndex() + "\t" +
				// resultEntity.getText() + "\t" + type.name() + "\t"
				// + resultEntity.getType().getConcept().conceptID + "\t1");

			}
			resultFile.println();
		}
		resultFile.close();
	}

	public void evaluateResult(List<SampledInstance<LabeledJlinkDocument, JLinkState, JLinkState>> predictions) {
		// Map<String, Set<String>> gold = new HashMap<String, Set<String>>();
		Map<String, Set<String>> goldRec = new HashMap<String, Set<String>>();
		Map<String, Set<String>> goldDisamb = new HashMap<String, Set<String>>();
		// Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		Map<String, Set<String>> resultRec = new HashMap<String, Set<String>>();
		Map<String, Set<String>> resultDisamb = new HashMap<String, Set<String>>();
		int unkCounter = 0;
		for (SampledInstance<LabeledJlinkDocument, JLinkState, JLinkState> prediction : predictions) {

			/*
			 * Split to get on documents name level instead of the sentence
			 * level.
			 */
			final String resultKey = prediction.getInstance().getName().split("-")[0];
			// result.putIfAbsent(resultKey, new HashSet<String>());
			resultRec.putIfAbsent(resultKey, new HashSet<String>());
			resultDisamb.putIfAbsent(resultKey, new HashSet<String>());

			for (EntityAnnotation resultEntity : prediction.getState().getEntities()) {
				if (!resultEntity.getType().equals(new Concept("MESH:-1", EEntityType.UNK))) {
					if (!resultEntity.getType().equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT)) {
						// result.get(resultKey).add(new
						// EntityRecAndDisambComparisonObject(resultEntity).toString());
						resultRec.get(resultKey).add(new EntityRecComparisonObject(resultEntity).toString());
						resultDisamb.get(resultKey).add(new EntityDisambComparisonObject(resultEntity).toString());

					} else {
						unkCounter++;
					}
				}
			}

			/*
			 * Split to get on documents name level instead of the sentence
			 * level.
			 */

			final String goldKey = prediction.getInstance().getName().split("-")[0];
			// gold.putIfAbsent(goldKey, new HashSet<String>());
			goldRec.putIfAbsent(goldKey, new HashSet<String>());
			goldDisamb.putIfAbsent(goldKey, new HashSet<String>());
			JLinkState goldState = prediction.getGoldResult();

			for (EntityAnnotation goldEntity : goldState.getEntities()) {
				if (!goldEntity.getType().equals(new Concept("MESH:-1", EEntityType.UNK))) {
					// gold.get(goldKey).add(new
					// EntityRecAndDisambComparisonObject(goldEntity).toString());
					goldRec.get(goldKey).add(new EntityRecComparisonObject(goldEntity).toString());
					goldDisamb.get(goldKey).add(new EntityDisambComparisonObject(goldEntity).toString());
				}
			}

		}
		System.out.println("resultRec = " + resultRec);
		Map<String, Integer> tpCounter = new HashMap<>();
		Map<String, Integer> fpCounter = new HashMap<>();
		Map<String, Integer> fnCounter = new HashMap<>();

		for (Entry<String, Set<String>> state : goldDisamb.entrySet()) {
			log.info(state.getKey());
			log.info("\tgold:\t");
			log.info(goldDisamb.get(state.getKey()));
			log.info("\tresult:\t");
			log.info(resultDisamb.get(state.getKey()));
			log.info("");

			List<String> x = new ArrayList<>(goldDisamb.get(state.getKey()));
			x.removeAll(resultDisamb.get(state.getKey()));
			List<String> y = new ArrayList<>(resultDisamb.get(state.getKey()));
			y.removeAll(goldDisamb.get(state.getKey()));

			List<String> z = new ArrayList<>(resultDisamb.get(state.getKey()));
			z.retainAll(goldDisamb.get(state.getKey()));
			log.info("Correct: " + z);
			log.info("Missing: " + x);
			log.info("To much: " + y);
			log.info("");

			for (String tp : z) {
				tpCounter.put(tp, tpCounter.getOrDefault(tp, 0) + 1);
			}
			for (String fn : x) {
				fnCounter.put(fn, fnCounter.getOrDefault(fn, 0) + 1);
			}
			for (String fp : y) {
				fpCounter.put(fp, fpCounter.getOrDefault(fp, 0) + 1);
			}

		}

		log.info("");

		List<Pair> tps = new ArrayList<>(tpCounter.entrySet().stream().map(e -> new Pair(e.getKey(), e.getValue()))
				.collect(Collectors.toList()));
		Collections.sort(tps);

		List<Pair> fps = new ArrayList<>(fpCounter.entrySet().stream().map(e -> new Pair(e.getKey(), e.getValue()))
				.collect(Collectors.toList()));
		Collections.sort(fps);

		List<Pair> fns = new ArrayList<>(fnCounter.entrySet().stream().map(e -> new Pair(e.getKey(), e.getValue()))
				.collect(Collectors.toList()));
		Collections.sort(fns);

		log.info("Most done True Positives:");
		tps.forEach(log::info);
		log.info("Most done False Positives:");
		fps.forEach(log::info);
		log.info("Most done False Negatives:");
		fns.forEach(log::info);

		results.print(unkCounter + "\t");

		// try {
		// log.info("Results of the recognition AND disambiguation:");
		// PRF1Extended.calculate(gold, result, true);
		// } catch (Exception e) {
		// }
		try {
			log.info("Results of the recognition WITHOUT disambiguation:");
			PRF1Extended.calculate(goldRec, resultRec, true);
		} catch (Exception e) {
		}

		try {
			log.info("Results of the disambiguation-set:");
			PRF1Extended.calculate(goldDisamb, resultDisamb, true);
		} catch (Exception e) {
		}

	}

	public static class Pair implements Comparable<Pair> {
		public String name;
		public int counter;

		public Pair(String name, int counter) {
			this.name = name;
			this.counter = counter;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + counter;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (counter != other.counter)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Pair [name=" + name + ", counter=" + counter + "]";
		}

		@Override
		public int compareTo(Pair o) {
			return -Integer.compare(counter, o.counter);
		}

	}
}
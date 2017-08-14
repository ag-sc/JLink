package main;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import corpus.DocumentCorpus;
import corpus.SimpleRegexTokenizer;
import corpus.Tokenization;
import exceptions.UnkownTemplateRequestedException;
import main.param.Parameter;
import main.setting.Setting;
import variables.JLinkState;
import variables.LabeledJlinkDocument;

public class Main {

	public static Logger log = LogManager.getFormatterLogger(Main.class.getSimpleName());

	private static final long version = 1;

	public static final boolean DEBUG = false;

	public final static boolean FILTER = false;

	public final static boolean FAKE_TEST_DATA = DEBUG;

	public static DocumentCorpus testCorpus() {
		DocumentCorpus corpus = new DocumentCorpus();
		final String docName = "TestDocument";

		List<String> originalSentences = Arrays.asList("patients (P) with heart-disease known .."// MESH:D006331
		);
		List<Tokenization> tokenizations = new SimpleRegexTokenizer().tokenize(originalSentences);

		for (int i = 0; i < originalSentences.size(); i++) {
			LabeledJlinkDocument doc = new LabeledJlinkDocument(docName, originalSentences.get(i),
					tokenizations.get(i).tokens);

			JLinkState priorKnowledge = new JLinkState(doc);
			JLinkState goldState = new JLinkState(priorKnowledge);

			doc.setGoldResult(goldState);

			corpus.addDocument(doc);
		}
		return corpus;

	}

	public static final EType type = EType.Disease;

	public static Parameter parameter;

	public static void main(String[] args) throws ClassNotFoundException, UnkownTemplateRequestedException, Exception {

		if (args == null || args.length == 0) {
			parameter = ParameterReader.defaultParameters();
		} else {
			parameter = ParameterReader.readParametersFromCommandLine(args);
		}

		log.info("Run parameter: " + parameter);

		Setting setting = buildSettings(parameter);

		log.info("Run setting: " + setting);

		JLink jlink = new JLink(setting);

		final long startTime = System.currentTimeMillis();

		switch (parameter.evaluationMode) {
		case TEST:
			jlink.startTestOnlyProcedure();
			break;
		case TRAIN_TEST:
			jlink.startTrainAndTestProcedure();
			break;
		case TRAIN:
			jlink.startTrainOnlyProcedure();
			break;
		case PREDICT:
			log.warn("Predicting mode is not yet implemented.");
			break;
		}

		log.info(
				"Time need to process : " + ((double) (System.currentTimeMillis() - startTime) / 60000d) + " minutes.");

		JLink.results.close();
	}

	public static Setting buildSettings(Parameter parameter) {
		return new Setting(version, parameter.personalNotes, parameter.alpha, parameter.scorerType, parameter.corpus,
				parameter.trainDataset, parameter.testDataset, DEBUG || FILTER, parameter.candidateRetrievalSetting,
				parameter.templateSetting, parameter.epochs, parameter.modelRootDirectory, parameter.evaluationMode);
	}

}
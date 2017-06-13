package baselines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import corpus.DataReader;
import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import main.JLink;
import main.Main;
import main.JLink.Pair;
import main.ParameterReader;
import main.evaluation.PRF1Extended;
import main.param.Parameter;
import main.setting.EDataset;
import tokenization.Tokenizer;

/**
 * This class contains a method that computes P. R. F1 given the training data,
 * develop data, medic and omim as dictionary.
 * 
 * Thereto, a dictionary is build using the training annotations and the underlying ontology.
 * 
 * It simply iterates over all words in the tokenized text and matches the
 * longest dictionary entry.
 * 
 * The result will give a upper bound for BIRE that can be achieved using only
 * direct match features.
 * 
 * @author hterhors
 * 
 *         @formatter:off
 * 
 * Without manual filter list
 * 
 *	// tp = 1245.0
	// fp = 407.0
	// fn = 207.0
	// Micro precision = 0.754
	// Micro recall = 0.857
	// Micro F1 = 0.802
	//
	// Macro precision = 0.788
	// Macro recall = 0.892
	// Macro F1 = 0.837
 *
 *
 * With manual filter list for water magnesium and mercury (most common simple errors)
 *
 *
 *
	//tp = 1245.0
	//fp = 313.0
	//fn = 207.0
	//Micro precision = 0.799
	//Micro recall = 0.857
	//Micro F1 = 0.827
	//
	//Macro precision = 0.833
	//Macro recall = 0.892
	//Macro F1 = 0.861
 *
 *         @formatter:on
 * 
 *         Jan 20, 2016
 */
public class ChemicalsDirectEntryMatchAndDisambiguation {

	/**
	 * The disease dictionary build from medic extended by train and develop
	 * data.
	 */
	public static CollectiveDictionary dict;

	public static void main(String[] args) throws Exception {

		Parameter parameter;

		if (args == null || args.length == 0) {
			parameter = ParameterReader.defaultParameters();
		} else {
			parameter = ParameterReader.readParametersFromCommandLine(args);
		}
		new JLink(Main.buildSettings(parameter));

		System.out.print("Read chemical dictioanry instance...");
		dict = CollectiveDictionaryFactory.getDiseaseChemicalInstance();
		System.out.println(" done.");

		/*
		 * <DocumentID, Set<DiseaseID>>
		 */
		Map<String, Set<String>> testAnnotationData;

		/*
		 * <DocumentID, DocumentText>
		 */
		Map<String, String> testDataText;

		/*
		 * Tokenized annotations
		 */
		// Map<String, Set<String>> tokenizedTestAnnotations;

		/*
		 * Tokenized documents
		 */
		Map<String, String> tokenizedTestDataText;

		/*
		 * Dictionary build from training data anotations.
		 */
		List<String> dictionary;

		/*
		 * This map stores for each document (Key) a list of disease surface
		 * forms (Value) that could be found in the tokenized text.
		 */
		Map<String, Set<String>> findings;

		final EDataset testSet = EDataset.DEVELOP;
		
		System.out.print("Load chemical annotations from " + testSet + "... ");
		testAnnotationData = DataReader.loadDiseaseChemicalAnnotationData(testSet, true, false);
		System.out.println(" done.");

		testDataText = DataReader.loadTexts(testSet);

		findings = new ConcurrentHashMap<String, Set<String>>();
		dictionary = generateDictionary(new HashSet<>());
		tokenizedTestDataText = new HashMap<String, String>();
		System.out.print("Generate dictionary... ");

		System.out.println(" done.");

		System.out.print("Sort dictionary...");
		sortDicitonary(dictionary);
		System.out.println(" done.");

		tokenizeGoldText(testDataText, tokenizedTestDataText);

		System.out.print("Find annotations...");
		findAnnotations(tokenizedTestDataText, dictionary, findings);
		System.out.println(" done.");
		findings.entrySet().forEach(System.out::println);

		compareResultsToGold(testAnnotationData, findings);

		PRF1Extended.calculate(testAnnotationData, findings);

		Map<String, Integer> tpCounter = new HashMap<>();
		Map<String, Integer> fpCounter = new HashMap<>();
		Map<String, Integer> fnCounter = new HashMap<>();

		for (Entry<String, Set<String>> state : testAnnotationData.entrySet()) {

			List<String> x = new ArrayList<>(testAnnotationData.get(state.getKey()));
			x.removeAll(findings.get(state.getKey()));
			List<String> y = new ArrayList<>(findings.get(state.getKey()));
			y.removeAll(testAnnotationData.get(state.getKey()));
			List<String> z = new ArrayList<>(findings.get(state.getKey()));
			z.retainAll(testAnnotationData.get(state.getKey()));

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

		List<String> rest = new ArrayList<>(fpCounter.keySet());
		rest.removeAll(tpCounter.keySet());
		System.out.println(rest);

		List<Pair> tps = new ArrayList<>(tpCounter.entrySet().stream().map(e -> new Pair(e.getKey(), e.getValue()))
				.collect(Collectors.toList()));
		Collections.sort(tps);

		List<Pair> fps = new ArrayList<>(fpCounter.entrySet().stream().map(e -> new Pair(e.getKey(), e.getValue()))
				.collect(Collectors.toList()));
		Collections.sort(fps);

		List<Pair> fns = new ArrayList<>(fnCounter.entrySet().stream().map(e -> new Pair(e.getKey(), e.getValue()))
				.collect(Collectors.toList()));
		Collections.sort(fns);

		System.out.println("Most done True Positives:");
		tps.forEach(System.out::println);
		System.out.println("Most done False Positives:");
		fps.forEach(System.out::println);
		System.out.println("Most done False Negatives:");
		fns.forEach(System.out::println);

	}

	private static void compareResultsToGold(Map<String, Set<String>> goldAnnotations,
			Map<String, Set<String>> findings) {
		/*
		 * Put set of findings into this list to sort them by name. (Just for
		 * visualization)
		 */
		List<String> findingsForDocID;

		/*
		 * Put set of gold annotations into this list to sort them by name.
		 * (Just for visualization)
		 */
		List<String> goldAnnotationsForDocID;

		/*
		 * Compare findings with gold annotation.
		 */
		for (Entry<String, Set<String>> finding : findings.entrySet()) {

			findingsForDocID = new ArrayList<String>(finding.getValue());
			goldAnnotationsForDocID = new ArrayList<String>(goldAnnotations.get(finding.getKey()));

			Collections.sort(findingsForDocID);
			Collections.sort(goldAnnotationsForDocID);

			System.out.println("DocumentID	= " + finding.getKey());
			System.out.println("Findings 	= " + findingsForDocID);
			System.out.println("Gold 		= " + goldAnnotationsForDocID);
			System.out.println("Precision: "
					+ PRF1Extended.macroPrecision(goldAnnotations.get(finding.getKey()), finding.getValue()));
			System.out.println(
					"Recall: " + PRF1Extended.macroRecall(goldAnnotations.get(finding.getKey()), finding.getValue()));
			System.out
					.println("F1: " + PRF1Extended.macroF1(goldAnnotations.get(finding.getKey()), finding.getValue()));
			System.out.println();

		}
	}

	/*
	 * Both
	 * 
	 * tp = 2867.0 fp = 698.0 fn = 601.0 Micro precision = 0.804 Micro recall =
	 * 0.827 Micro F1 = 0.815
	 * 
	 * Macro precision = 0.813 Macro recall = 0.834 Macro F1 = 0.823
	 * 
	 */

	private static void findAnnotations(Map<String, String> tokenizedTestDataText, List<String> dictionary,
			Map<String, Set<String>> findings) {

		/*
		 * Find annotations
		 * 
		 * Remove findings from the text.
		 */

		tokenizedTestDataText.entrySet().stream().parallel().forEach(doc -> {
			System.out.print(".");

			/*
			 * Search pattern for disease surface forms in tokenized text.
			 */
			Pattern p;

			/*
			 * Patterns matcher.
			 */
			Matcher m;

			/*
			 * Found match from the pattern provided by the matcher.
			 */
			String match;
			/*
			 * The text from the tokenized gold document.
			 */
			String text;

			/*
			 * The finding converted into diseaseID using the dictionary.
			 */
			String diseaseID = null;

			/*
			 * The found disease surface form.
			 */
			String diseaseSurfaceForm;

			text = doc.getValue();
			findings.put(doc.getKey(), new HashSet<String>());

			for (String dictEntry : dictionary) {

				p = Pattern.compile("((^| )" + Pattern.quote(dictEntry) + "( |$)){1}");
				m = p.matcher(text);

				if (m.find()) {

					match = m.group(1);

					text = text.replaceAll(match.trim() + "( |$)", "");
					/*-
					 * 1) Convert match to ID:
					 * 
					 * 2) Convert ID to preferred ID if ID is an alternate ID.
					 * 
					 * 3) Add match to findings.
					 */
					diseaseSurfaceForm = match.trim();

					// diseaseSurfaceForm =
					// dict.toSortedSyn(diseaseSurfaceForm);

					Set<Concept> concepts = dict.getConceptsForNormalizedSurfaceForm(diseaseSurfaceForm);

					for (Concept concept : concepts) {
						diseaseID = concept.getConceptID();
						break;
					}

					/*
					 * Old dictionary...// diseaseID =
					 * dict.mapToPrefIDIfAny(diseaseID);
					 */

					/*
					 * Break if there
					 */
					if (diseaseID == null) {
						System.out.println("Unkwon disease ID for surface form = " + diseaseSurfaceForm);
						break;
					}

					findings.get(doc.getKey()).add(diseaseID);
				}
			}
		});
		System.out.println(" ...finished");
	}

	private static void tokenizeGoldText(Map<String, String> testDataText, Map<String, String> tokenizedTestDataText) {
		/*
		 * Tokenize test data text.
		 */
		for (Entry<String, String> doc : testDataText.entrySet()) {
			tokenizedTestDataText.put(doc.getKey(), Tokenizer.getTokenizedForm(doc.getValue()));
		}
	}

	private static void sortDicitonary(List<String> dictionary) {
		/*
		 * Sort list by length to take max lenght match first.
		 */
		Collections.sort(dictionary, (o1, o2) -> -Integer.compare(o1.length(), o2.length()));
	}

	private static void tokenizeGoldAnnotations(Map<String, Set<String>> testAnnotationData,
			Map<String, Set<String>> tokenizedTestAnnotations) {
		/*
		 * Tokenized annotation.
		 */
		String annotation;

		/*
		 * Tokenize test annotations to make them comparable with the findings.
		 */
		for (Entry<String, Set<String>> doc : testAnnotationData.entrySet()) {
			tokenizedTestAnnotations.put(doc.getKey(), new HashSet<String>());

			for (String ann : doc.getValue()) {
				annotation = Tokenizer.getTokenizedForm(ann);

				tokenizedTestAnnotations.get(doc.getKey()).add(annotation);
			}
		}
	}

	private static void generateDictionary(Map<String, Set<String>> trainAnnotationData, List<String> dictionary) {

		/*
		 * Tokenized annotation that is stored in the tokenized annotations map.
		 */
		String annotation;

		/*
		 * Tokenize annotation data
		 *
		 * and
		 *
		 * Create dictionary
		 */
		for (Entry<String, Set<String>> doc : trainAnnotationData.entrySet()) {

			for (String ann : doc.getValue()) {
				annotation = Tokenizer.getTokenizedForm(ann);

				if (!dictionary.contains(annotation))
					dictionary.add(annotation);
			}
		}
	}

	private static List<String> generateDictionary(Set<String> dictionary) {

		List<String> list = new ArrayList<>();

		/*
		 * Dictionary preferred entry;
		 */

		for (String dictEntry : dict.getAllSurfaceForms()) {

			// if (dictEntry.toLowerCase().equals("water"))
			// continue;
			// if (dictEntry.toLowerCase().equals("hg"))
			// continue;
			// if (dictEntry.toLowerCase().equals("hormone"))
			// continue;
			// if (dictEntry.toLowerCase().equals("hormones"))
			// continue;
			// if (dictEntry.toLowerCase().equals("mg"))
			// continue;

			// if (dictEntry.length() <= 120)
			dictionary.add(dictEntry);

		}
		list.addAll(dictionary);
		System.out.println("dictionary size = " + dictionary.size());
		return list;

	}

}

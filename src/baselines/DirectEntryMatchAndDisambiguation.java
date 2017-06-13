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
 * develop data, medic as dictionary.
 * 
 * Thereto, a dictionary is build using the training annotations.
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
 *         Results:
 *
 *          PFR1 without any Conjunctions / Disjunctions (Both count as False negative): 
 *          tp = 251.0
 *          fp = 45.0
 *          fn = 85.0 
 *         
 *          Micro precision = 0.848 
 *          Micro recall = 0.747 
 *          Micro F1 = 0.794
 *          
 *          Macro precision = 0.868 
 *          Macro recall = 0.796 
 *          Macro F1 = 0.814
 *
 *          PFR1 without any Conjunctions / Disjunctions (Both count as False negative). Not allowing Alternate ID mapping: 
 *			tp = 241.0
 *			fp = 55.0
 *			fn = 95.0
 *			Micro precision = 0.814
 *			Micro recall = 0.717
 *			Micro F1 = 0.763
 *			
 *			Macro precision = 0.833
 *			Macro recall = 0.762
 *			Macro F1 = 0.78
 *
 *
 *          
 *          
 *          
 *          PFR1Extended: Allowing conjunctions. Disjunctions does count as False negative: 
 *			tp = 253.0
 *			fp = 37.0
 *			fn = 77.0
 *
 *			Micro precision = 0.872
 *			Micro recall = 0.767
 *			Micro F1 = 0.816
 *			
 *			Macro precision = 0.872
 *			Macro recall = 0.809
 *			Macro F1 = 0.824
 *			
 *
 *          PFR1Extended: Allowing conjunctions. Disjunctions does count as False negative. Not allowing Alternate ID mapping: 
 *			tp = 243.0
 *			fp = 48.0
 *			fn = 88.0
 *			Micro precision = 0.835
 *			Micro recall = 0.734
 *			Micro F1 = 0.781
 *			
 *			Macro precision = 0.837
 *			Macro recall = 0.775
 *			Macro F1 = 0.789
 *
 *
 *
 *
 *         @formatter:on
 * 
 *         Jan 20, 2016
 */
public class DirectEntryMatchAndDisambiguation {

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
		dict = CollectiveDictionaryFactory.getInstance();

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

		testAnnotationData = DataReader.loadDiseaseAnnotationData(testSet, true, false);

		testDataText = DataReader.loadTexts(testSet);

		findings = new ConcurrentHashMap<String, Set<String>>();
		dictionary = new ArrayList<String>();
		tokenizedTestDataText = new HashMap<String, String>();

		generateDictionary(dictionary);

		sortDicitonary(dictionary);

		tokenizeGoldText(testDataText, tokenizedTestDataText);

		findAnnotations(tokenizedTestDataText, dictionary, findings);

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

	private static void generateDictionary(List<String> dictionary) {

		/*
		 * Dictionary preferred entry;
		 */

		for (String dictEntry : dict.getAllSurfaceForms()) {

			if (applyFilter(dictEntry))
				if (!dictionary.contains(dictEntry))
					dictionary.add(dictEntry);

		}
		System.out.println("dictionary size = " + dictionary.size());
		// dictionary.forEach(System.out::println);

	}

	private static boolean applyFilter(String n) {

		// if (n.length() <= 3)
		// return false;
		//
		// if (StringUtil.isUpperCase(n))
		// return false;
		//
		// if (n.equals("disease"))
		// return false;
		//
		// if (n.equals("syndrome"))
		// return false;
		//
		// if (n.equals("diseases"))
		// return false;
		//
		// if (n.equals("syndromes"))
		// return false;
		//
		// if (n.equals("disorder"))
		// return false;
		//
		// if (n.equals("dysfunction"))
		// return false;
		//
		// if (n.equals("disorders"))
		// return false;
		//
		// if (n.equals("dysfunctions"))
		// return false;
		//
		// if (n.equals("tumorigenesis"))
		// return false;
		//
		// if (n.equals("haemolysis"))
		// return false;
		//
		// if (n.equals("thrombocytopenic"))
		// return false;
		//
		// if (n.equals("fever"))
		// return false;
		//
		// if (n.equals("convulsions"))
		// return false;
		//
		// if (n.equals("infections"))
		// return false;
		//
		// if (n.equals("chromosomal translocations"))
		// return false;
		//
		// if (n.equals("strains"))
		// return false;
		//
		// if (n.equals("inflammation"))
		// return false;

		return true;
	}
}

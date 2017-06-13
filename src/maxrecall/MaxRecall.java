package maxrecall;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.lucene.queryparser.classic.ParseException;

import candidateretrieval.jaccard.JaccardRetrieval;
import candidateretrieval.levenshtein.LevenshteinRetrieval;
import candidateretrieval.lucene.LuceneRetrieval;
import corpus.DataReader;
import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import main.EType;
import main.JLink;
import main.Main;
import main.ParameterReader;
import main.evaluation.PRF1Extended;
import main.param.Parameter;
import main.setting.EDataset;
import tokenization.Tokenizer;

public class MaxRecall {

	public static int LUCENE_MAX_RESULTS = 1;
	public static double LUCENE_SIMILARITY_TRESHOLD = 0.7d;

	final public static double NGRAM_SIMILARITY_TRESHOLD = 0.7d;
	private static final int MAX_NGRAM_RESULTS = 10;

	final public static double LEVENSHTEIN_SIMILARITY_TRESHOLD = 0.8d;
	private static final int LEVENSHTEIN_MAX_DISTANCE = 1;

	private static CollectiveDictionary dict;
	private static JaccardRetrieval NgramRetrieval;
	private static LevenshteinRetrieval levenshteinRetrieval;
	private static LuceneRetrieval luceneRetrieval;

	public static void main(String[] args) throws IOException, ParseException {

		Parameter parameter;

		if (args == null || args.length == 0) {
			parameter = ParameterReader.defaultParameters();
		} else {
			parameter = ParameterReader.readParametersFromCommandLine(args);
		}
		new JLink(Main.buildSettings(parameter));
		dict = CollectiveDictionaryFactory.getInstance();
		int j = 0;
		double i = 0;
		for (String string : dict.getAllSurfaceForms()) {
			i += dict.getConceptsForNormalizedSurfaceForm(string).size();
			if (dict.getConceptsForNormalizedSurfaceForm(string).size() != 1) {
				System.out.println(string);
				System.out.println(dict.getConceptsForNormalizedSurfaceForm(string));
				j++;
			}
		}
		System.out.println(dict.getAllSurfaceForms().size() / i);
		System.out.println("j = " + j);
		System.out.println(dict.getAllSurfaceForms().size());
		System.exit(1);
		luceneRetrieval = LuceneRetrieval.getInstance();

		// System.out.println(
		// luceneRetrieval.getFuzzyCandidates("methamphetamine",
		// LUCENE_MAX_RESULTS, LUCENE_SIMILARITY_TRESHOLD));
		//
		// System.exit(1);

		// NgramRetrieval = JaccardRetrieval.getInstance();
		// levenshteinRetrieval = LevenshteinRetrieval.getInstance();

		// String set = "testset";

		EDataset set = EDataset.TEST;

		Map<String, Set<String>> goldData = DataReader.loadChemicalAnnotationData(set, true, true);

		Map<String, Set<String>> annotations = DataReader.loadChemicalAnnotationData(set, false, false);

		if (JLink.type == EType.Chemical) {

			goldData = DataReader.loadChemicalAnnotationData(set, true, true);

			annotations = DataReader.loadChemicalAnnotationData(set, false, false);
		} else if (JLink.type == EType.Disease) {

			goldData = DataReader.loadDiseaseAnnotationData(set, true, true);

			annotations = DataReader.loadDiseaseAnnotationData(set, false, false);

		}

		// Map<String, Set<String>> resultIDF =
		// calculateResultIDF(annotations);

		Map<String, Set<Concept>> results = getIDs(annotations);

		Map<String, Set<String>> simpleResult = new HashMap<>();

		for (Entry<String, Set<Concept>> resultEntry : results.entrySet()) {

			simpleResult.putIfAbsent(resultEntry.getKey(), new HashSet<>());
			simpleResult.get(resultEntry.getKey())
					.addAll(resultEntry.getValue().stream().map(c -> c.conceptID).collect(Collectors.toList()));
		}

		PRF1Extended.calculate(goldData, simpleResult);

		analyze(goldData, simpleResult);

	}

	private static void analyze(Map<String, Set<String>> gold, Map<String, Set<String>> annotations) {
		for (Entry<String, Set<String>> doc : gold.entrySet()) {
			System.out.println("Document : " + doc.getKey());
			for (String goldAnn : doc.getValue()) {
				if (!annotations.get(doc.getKey()).contains(goldAnn)) {
					System.out.println("\t" + goldAnn);
				}

			}

		}
	}

	private static Map<String, Set<Concept>> getIDs(Map<String, Set<String>> annotations) {

		Map<String, Set<Concept>> result = new ConcurrentHashMap<String, Set<Concept>>();

		annotations.entrySet().parallelStream().forEach(doc -> {

			result.put(doc.getKey(), new HashSet<>());

			doc.getValue().stream().forEach(ann -> {
				ann = Tokenizer.getTokenizedForm(ann);
				result.get(doc.getKey()).addAll(dict.getConceptsForNormalizedSurfaceForm(ann));

				result.get(doc.getKey())
						.addAll(luceneRetrieval.getFuzzyCandidates(ann, LUCENE_MAX_RESULTS, LUCENE_SIMILARITY_TRESHOLD)
								.stream().map(c -> c.concept).collect(Collectors.toList()));

				// System.out
				// .println(ann + " : "
				// + luceneRetrieval
				// .getFuzzyCandidates(ann, LUCENE_MAX_RESULTS,
				// LUCENE_SIMILARITY_TRESHOLD)
				// .stream().map(c ->
				// c.concept).collect(Collectors.toList()).size());

				// result.get(doc.getKey())
				// .addAll(NgramRetrieval.getCandidates(ann, MAX_NGRAM_RESULTS,
				// NGRAM_SIMILARITY_TRESHOLD).stream()
				// .map(c -> c.concept).collect(Collectors.toList()));
				// result.get(doc.getKey())
				// .addAll(levenshteinRetrieval
				// .getCandidates(ann, LEVENSHTEIN_MAX_DISTANCE,
				// LEVENSHTEIN_SIMILARITY_TRESHOLD).stream()
				// .map(c -> c.concept).collect(Collectors.toList()));

			});

		});
		return result;
	}

}

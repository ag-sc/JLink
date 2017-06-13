package tfidf;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IDFDataAccess {

	public static void main(String[] args) throws IOException {

		// init();
		// long t = System.currentTimeMillis();
		//
		// System.out.println("Get highest Score...");
		// System.out.println(getBestIDForTerms(Arrays.asList(Tokenizer
		// .stanfordTokenization("fish-eye disease").split(" ")), 100));
		// System.out.println(getBestIDForTerms(Arrays.asList(Tokenizer
		// .stanfordTokenization("myotonia").split(" ")), 100));
		// System.out.println(getBestIDForTerms(Arrays.asList(Tokenizer
		// .stanfordTokenization("SJS").split(" ")), 100));
		// System.out.println(getBestIDForTerms(Arrays.asList(Tokenizer
		// .stanfordTokenization("HD").split(" ")), 100));
		// System.out.println(" finished");
		// System.out.println("Time needed = " + (System.currentTimeMillis() -
		// t));

	}

	/*
	 * DiseaseID, Term,Frequency
	 */
	final static private Map<String, Map<String, Double>> allTFs = new HashMap<String, Map<String, Double>>();
	final static private Map<String, Double> allIDFs = new HashMap<String, Double>();

	// public static void init() {
	//
	// dict = DiseaseDictionaryFactory.getInstance(false,
	// ETokenizationType.SIMPLE, NCBI_DiseaseLearning.INCLUDE_TRAIN,
	// false, false, NCBI_DiseaseLearning.INCLUDE_OMIM);
	//
	// Map<String, List<String>> documents = new HashMap<String,
	// List<String>>();
	//
	// /*
	// * Calc TFs
	// */
	// System.out.println("Calculating TFs...");
	// for (Entry<MEDICDisease, Set<String>> disease :
	// dict.getDictionary().entrySet()) {
	//
	// List<String> document = new ArrayList<String>();
	//
	// document.addAll(Arrays.asList(disease.getKey().normalizedForm.split("
	// ")));
	//
	// for (String syn : disease.getValue()) {
	// document.addAll(Arrays.asList(syn.split(" ")));
	// }
	//
	// documents.put(disease.getKey().diseaseID, document);
	// allTFs.put(disease.getKey().diseaseID, TFIDF.getTFs(document, false));
	// }
	//
	// // allTFs.entrySet().forEach(System.out::println);
	//
	// System.out.println(" finished.");
	// /*
	// * Get IDFS
	// */
	// System.out.println("Calculating IDFs...");
	// allIDFs.putAll(TFIDF.getIDFs(documents));
	// System.out.println(" finished.");
	//
	// // allIDFs.entrySet().forEach(System.out::println);
	//
	// }

	public static final List<Entry<String, Double>> getBestIDForTerm(final String term, final int numOfDocuments) {

		String search = term.toLowerCase();
		// System.out.println("Search for term list: " + terms);

		/*
		 * diseaseID , sum of tfidfs
		 */

		final Map<String, Double> result = new ConcurrentHashMap<String, Double>();

		// dict.getMEDICDiseaseIDs().parallelStream().forEach(diseaseID -> {
		// synchronized (result) {
		// result.put(diseaseID, result.getOrDefault(diseaseID, 0d) +
		// getIDF(diseaseID, search));
		//
		// }
		// });

		final List<Map.Entry<String, Double>> res = result.entrySet().parallelStream()
				.sorted(new Comparator<Entry<String, Double>>() {
					@Override
					public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
						return -Double.compare(o1.getValue(), o2.getValue());
					}
				}).collect(Collectors.toList());

		return res.subList(0, Math.min(res.size(), numOfDocuments));
	}

	public static final List<Entry<String, Double>> getBestIDForTerms(List<String> terms, final int numOfDocuments) {

		// System.out.println("Search for term list: " + terms);

		/*
		 * diseaseID , sum of tfidfs
		 */

		final Map<String, Double> result = new ConcurrentHashMap<String, Double>();
		for (String term : terms) {

			String search = term.toLowerCase();

			// dict.getMEDICDiseaseIDs().parallelStream().forEach(diseaseID -> {
			// synchronized (result) {
			// result.put(diseaseID, result.getOrDefault(diseaseID, 0d) +
			// getIDF(diseaseID, search));
			//
			// }
			// });
		}

		final List<Map.Entry<String, Double>> res = result.entrySet().stream()
				.sorted(new Comparator<Entry<String, Double>>() {
					@Override
					public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
						return -Double.compare(o1.getValue(), o2.getValue());
					}
				}).filter(e -> e.getValue() > 0).collect(Collectors.toList());

		return res.subList(0, Math.min(res.size(), numOfDocuments));
	}

	private static synchronized double getIDF(String documentName, String term) {
		return (allTFs.get(documentName).getOrDefault(term, 0d) == 0 ? 0 : 1) * allIDFs.getOrDefault(term, 0d);
	}
}

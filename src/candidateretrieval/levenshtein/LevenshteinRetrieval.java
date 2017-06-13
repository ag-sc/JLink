package candidateretrieval.levenshtein;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import candidateretrieval.ICandidateRetrieval;
import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import dictionary.DictionaryEntry;
import dictionary.subdicts.ISubDictionary;
import metric.LevenShteinSimilarities;

public class LevenshteinRetrieval implements ICandidateRetrieval {

	final public static double SIMILARITY_TRESHOLD = 0.8d;
	public static final int MAX_DISTANCE = 1;

	private CollectiveDictionary dict;

	private Map<String, List<LevenshteinCandidate>> cache = new ConcurrentHashMap<>();

	private static LevenshteinRetrieval retrieval = null;

	public static LevenshteinRetrieval getInstance() {

		if (retrieval == null)
			retrieval = new LevenshteinRetrieval();

		return retrieval;
	}

	public static void main(String[] args) {
		LevenshteinRetrieval r = LevenshteinRetrieval.getInstance();

		r.getCandidates("leukaemia").forEach(System.out::println);
	}

	private LevenshteinRetrieval() {
		CollectiveDictionary dict = CollectiveDictionaryFactory.getInstance();
		System.out.print("Prepare LevenShteinCandidateRetrieval component... ");

		this.dict = dict;

		System.out.println(" done.");

	}

	public List<LevenshteinCandidate> getCandidates(final String surfaceForm) {
		return getCandidates(surfaceForm, MAX_DISTANCE, SIMILARITY_TRESHOLD);
	}

	public List<LevenshteinCandidate> getCandidates(final String surfaceForm, int levenshteinMaxDistance,
			double levenshteinSimilarityTreshold) {

		if (cache.containsKey(surfaceForm))
			return cache.get(surfaceForm);

		List<LevenshteinCandidate> possibleDiseaseIDs = new ArrayList<>();

		for (ISubDictionary subDict : dict.getDictionaries()) {
			for (Entry<DictionaryEntry, Set<Concept>> entry : subDict.getDictionary().entrySet()) {
				final Set<Concept> concepts = entry.getValue();

				final String dictEntry = entry.getKey().normalizedSurfaceForm;

				final double dist = Math.abs(dictEntry.length() - surfaceForm.length());

				if (dist > levenshteinMaxDistance)
					continue;

				final boolean matchesLengthCondition = Math.pow(
						1 - Math.pow(dist / Math.max(dictEntry.length(), surfaceForm.length()), 2),
						2) >= levenshteinSimilarityTreshold;

				if (!matchesLengthCondition)
					continue;

				final double levenshteinSimilarity = LevenShteinSimilarities.weightedLevenshteinSimilarity(surfaceForm, dictEntry,
						levenshteinMaxDistance);

				if (levenshteinSimilarity >= levenshteinSimilarityTreshold) {
					for (Concept concept : concepts) {
						possibleDiseaseIDs.add(new LevenshteinCandidate(concept, levenshteinSimilarity));
					}
				}

			}
		}

		cache.put(surfaceForm, possibleDiseaseIDs);

		return possibleDiseaseIDs;

	}

}

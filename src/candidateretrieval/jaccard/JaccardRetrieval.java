package candidateretrieval.jaccard;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import metric.JaccardSimilarities;
import util.StringUtils;

public class JaccardRetrieval implements ICandidateRetrieval {
	final public static double NGRAM_SIMILARITY_TRESHOLD = 0.5d;
	private static final int MAX_NGRAM_RESULTS = 10;

	private final int N_GRAM_SIZE = 3;

	/**
	 * setOfnGrams, diseaseID
	 */
	private final Map<BitSet, Set<Concept>> ngrammap;

	/**
	 * ngram, Set of setOfnGrams
	 */
	private final Map<Integer, Set<BitSet>> fastMapping;

	private Map<String, List<JaccardCandidate>> cache = new ConcurrentHashMap<>();

	private static JaccardRetrieval retrieval = null;

	/*
	 * NGram index mapping.
	 */
	public static Map<String, Integer> ngramIndex = new HashMap<>();

	public static void main(String[] args) {

		JaccardRetrieval j = JaccardRetrieval.getInstance();

		// CollectiveDictionary d = CollectiveDictionaryFactory.getInstance();
		//
		// int c = 0;
		// long t = System.currentTimeMillis();
		// for (String string : d.getAllSurfaceForms()) {
		// System.out.println(c++ + ": " + string + ": " +
		// j.getCandidates(string).size());
		// if (c == 1000)
		// break;
		// }
		// System.out.println("time = " + (System.currentTimeMillis() - t));

		long t = System.currentTimeMillis();
		j.getCandidates("leukaemia").forEach(System.out::println);
		System.out.println("time = " + (System.currentTimeMillis() - t));

	}

	public static JaccardRetrieval getInstance() {

		if (retrieval == null)
			retrieval = new JaccardRetrieval();

		return retrieval;
	}

	private JaccardRetrieval() {
		CollectiveDictionary dict = CollectiveDictionaryFactory.getInstance();
		System.out.print("Prepare JaccardRetrieval component... ");
		ngrammap = new HashMap<>();
		fastMapping = new HashMap<>();

		Map<Set<String>, Set<Concept>> ngramsString = new HashMap<>();

		Set<String> allNGrams = new HashSet<>();

		for (ISubDictionary subDict : dict.getDictionaries()) {
			for (Entry<DictionaryEntry, Set<Concept>> entry : subDict.getDictionary().entrySet()) {
				final Set<Concept> concepts = entry.getValue();

				final String prefForm = StringUtils.toLowercaseIfNotUppercase(entry.getKey().normalizedSurfaceForm);

				final Set<String> grams = getBagOfNGram(prefForm, N_GRAM_SIZE);

				allNGrams.addAll(grams);

				ngramsString.put(grams, concepts);

			}
		}

		int i = 0;
		for (String ngram : allNGrams) {
			ngramIndex.put(ngram, i++);
		}

		for (Entry<Set<String>, Set<Concept>> e : ngramsString.entrySet()) {
			BitSet ngramsBitSet = toBitSet(e.getKey());
			ngrammap.put(ngramsBitSet, e.getValue());
			for (String ngram : e.getKey()) {
				// fastMapping.putIfAbsent(ngram, new HashSet<>());
				// fastMapping.get(ngram).add(ngramsBitSet);
			}
		}

		System.out.println(" done.");

	}

	private BitSet toBitSet(Set<String> grams) {

		BitSet bitSet = new BitSet(ngramIndex.size());
		for (String ngram : grams) {
			if (ngramIndex.containsKey(ngram))
				bitSet.set(ngramIndex.get(ngram), true);
		}
		return bitSet;

	}

	public List<JaccardCandidate> getCandidates(String annotation) {
		return getCandidates(annotation, MAX_NGRAM_RESULTS, NGRAM_SIMILARITY_TRESHOLD);
	}

	public List<JaccardCandidate> getCandidates(String annotation, final int topK, final double treshold) {

		annotation = StringUtils.toLowercaseIfNotUppercase(annotation);

		if (cache.containsKey(annotation))
			return cache.get(annotation);

		final BitSet bitSetGrams = getBitSetOfNGram(annotation, N_GRAM_SIZE);

		List<JaccardCandidate> ids = new ArrayList<>();

		ngrammap.entrySet().stream().forEach(entry -> {

			final double js = JaccardSimilarities.jaccardSimilarity(entry.getKey(), bitSetGrams);
			if (js >= treshold) {
				for (Concept jaccardCandidateConcept : entry.getValue()) {
					ids.add(new JaccardCandidate(jaccardCandidateConcept, js));
				}
			}
		});
		Collections.sort(ids);

		cache.put(annotation, ids);

		return ids.subList(0, Math.min(ids.size(), topK));
	}

	private BitSet getBitSetOfNGram(final String text, final int charachterNGramSize) {

		BitSet bongam = new BitSet(ngramIndex.size());

		for (int i = 0; i < text.length() - (charachterNGramSize - 1); i++) {
			String ngram = text.substring(i, i + charachterNGramSize).intern();
			if (ngramIndex.containsKey(ngram))
				bongam.set(ngramIndex.get(ngram), true);
		}

		return bongam;
	}

	public static Set<String> getBagOfNGram(final String text, final int charachterNGramSize) {

		Set<String> bongam = new HashSet<String>();

		for (int i = 0; i < text.length() - (charachterNGramSize - 1); i++) {
			String nGramS = text.substring(i, i + charachterNGramSize).intern();
			bongam.add(nGramS);
		}

		return bongam;
	}
}

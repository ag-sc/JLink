package sampler;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import candidateretrieval.Stopwords;
import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import dictionary.DictionaryEntry;
import dictionary.subdicts.ISubDictionary;
import main.JLink;
import main.Main;
import main.ParameterReader;
import main.param.Parameter;
import metric.LevenShteinSimilarities;
import util.StringUtils;

public class ExtractSynonymsAndRules {

	private static Map<Concept, Set<String>> dictionary = new HashMap<>();

	public static void main(String[] args) throws IOException {
		Parameter parameter;

		if (args == null || args.length == 0) {
			parameter = ParameterReader.defaultParameters();
		} else {
			parameter = ParameterReader.readParametersFromCommandLine(args);
		}
		new JLink(Main.buildSettings(parameter));

		Map<String, Map<String, Double>> syns = generateSynonymTokens();

		syns.entrySet().forEach(System.out::println);
		int i = 0;

		for (Entry<String, Map<String, Double>> string : syns.entrySet()) {

			i += string.getValue().size();
		}
		System.out.println(i);
		System.exit(1);

		// final Set<CharacterRule> rules = new HashSet<>();

		// dict = (DiseaseDictionary)
		// IDFDictionaryCheck.restoreData("gen/medic_train_lemmatized_tokenized_dict.bin");
		//
		// dict.getDictionary().entrySet().stream().map(entry -> {
		//
		// Set<String> tokens = new HashSet<>();
		//
		// tokens.add(entry.getKey().normalizedForm);
		// tokens.addAll(entry.getValue());
		// return tokens;
		//
		// }).forEach(entries -> {
		// entries.forEach(entry1 -> {
		// entries.forEach(entry2 -> {
		// if (!entry1.equals(entry2)) {
		//
		// CharacterRule rule = DictLookUpHelper.extractCharRules(entry1,
		// entry2, 5);
		//
		// if (rule != null) {
		// rules.add(rule);
		// }
		// }
		// });
		// });
		// });

	}

	public static Map<String, Map<String, Double>> generateSynonymTokens() throws IOException {

		CollectiveDictionary dict = CollectiveDictionaryFactory.getInstance();

		/*
		 * Dictionary preferred entry;
		 */
		for (ISubDictionary subDict : dict.getDictionaries()) {
			for (Entry<DictionaryEntry, Set<Concept>> entry : subDict.getDictionary().entrySet()) {
				for (Concept concept : entry.getValue()) {
					dictionary.putIfAbsent(concept, new HashSet<String>());
					dictionary.get(concept).add(entry.getKey().normalizedSurfaceForm);
				}
			}
		}

		// dict.getDictionary().entrySet().forEach(System.out::println);

		Map<String, Map<String, Double>> synonymTokens = new HashMap<>();

		/*
		 * DiseaseID, Set of SurfaceForms
		 */

		for (Entry<Concept, Set<String>> entry : dictionary.entrySet()) {

			/*
			 * final String contextID = entry.getKey();
			 */

			Map<Integer, String> indexMapping = new HashMap<Integer, String>();
			Map<String, Integer> tokenMapping = new HashMap<String, Integer>();

			Set<String> words = new HashSet<String>();
			for (String syn : entry.getValue()) {
				words.addAll(Arrays.asList(syn.split(" ")));
			}

			int index = 0;
			for (String token : words) {
				indexMapping.put(index, token);
				tokenMapping.put(token, index);
				index++;
			}

			Set<String> indexStringsForSyns = new HashSet<String>();
			for (String syn : entry.getValue()) {
				if (syn.split(" ").length == 1) {
					continue;
				}
				indexStringsForSyns.add(convertToIndexString(tokenMapping, syn));
			}

			Map<String, Map<String, Double>> localSynonymTokens = new HashMap<>();

			for (String indexStringA : indexStringsForSyns) {
				for (String indexStringB : indexStringsForSyns) {

					if (indexStringA.equals(indexStringB))
						continue;

					String[] indexArrayA = indexStringA.split(" ");
					String[] indexArrayB = indexStringB.split(" ");

					if (indexArrayA.length == 1 || indexArrayB.length == 1)
						continue;

					if (indexArrayA.length == indexArrayB.length) {
						if (LevenShteinSimilarities.levenshteinDistance(indexStringA, indexStringB, 1) != 1) {
							continue;
						}

						final int exchangeableWordIndex = extractExchangeableWordIndex(indexArrayA, indexArrayB);

						final String exchangeableWordA = indexMapping
								.get(Integer.parseInt(indexArrayA[exchangeableWordIndex]));
						final String exchangeableWordB = indexMapping
								.get(Integer.parseInt(indexArrayB[exchangeableWordIndex]));

						if (exchangeableWordA.length() == 1)
							continue;

						if (exchangeableWordB.length() == 1)
							continue;

						if (Stopwords.ENGLISH_STOP_WORDS.contains(exchangeableWordA)) {
							continue;
						}
						if (Stopwords.ENGLISH_STOP_WORDS.contains(exchangeableWordB)) {
							continue;
						}
						synonymTokens.putIfAbsent(exchangeableWordA, new HashMap<String, Double>());
						localSynonymTokens.putIfAbsent(exchangeableWordA, new HashMap<String, Double>());

						final double contribution = 1d;
						// final double contribution = 1d /
						// indexStringsForSyns.size();
						localSynonymTokens.get(exchangeableWordA).put(exchangeableWordB, contribution);
						/*
						 * synonymTokens.putIfAbsent(contextID + "_" +
						 * exchangeableWordA, new HashMap<String, Integer>());
						 * 
						 * synonymTokens.get(contextID + "_" +
						 * exchangeableWordA).put(contextID + "_" +
						 * exchangeableWordB, synonymTokens.get(contextID + "_"
						 * + exchangeableWordA) .getOrDefault(contextID + "_" +
						 * exchangeableWordB, 0) + 1);
						 */
					}
				}
			}

			for (Entry<String, Map<String, Double>> a : localSynonymTokens.entrySet()) {
				final String exchangeableWordA = a.getKey();
				for (Entry<String, Double> b : a.getValue().entrySet()) {
					final String exchangeableWordB = b.getKey();

					synonymTokens.get(exchangeableWordA).put(exchangeableWordB,
							synonymTokens.get(exchangeableWordA).getOrDefault(exchangeableWordB, 0d) + 1);
				}

			}

		}

		/*
		 * Remove all terms that occur only once.
		 */
		final Map<String, Map<String, Double>> filteredSynonymTokens = new HashMap<>();

		final int MIN_NUM_OF_FREQ = 1;

		for (Entry<String, Map<String, Double>> e : synonymTokens.entrySet()) {

			final Map<String, Double> n = new HashMap<>();
			for (Entry<String, Double> e2 : e.getValue().entrySet()) {
				if (e2.getValue() > MIN_NUM_OF_FREQ)
					n.put(e2.getKey(), e2.getValue());
			}
			if (!n.isEmpty())
				filteredSynonymTokens.put(e.getKey(), n);

		}

		/*
		 * Normalize occurrences
		 */
		final Map<String, Map<String, Double>> normedSynonymTokens = new HashMap<>();

		for (Entry<String, Map<String, Double>> e : filteredSynonymTokens.entrySet()) {
			final double sum = e.getValue().values().stream().reduce(0d, (a, b) -> a + b);

			final Map<String, Double> n = new LinkedHashMap<>();
			for (Entry<String, Double> e2 : e.getValue().entrySet()) {
				n.put(e2.getKey(), e2.getValue() / sum);
			}

			normedSynonymTokens.put(e.getKey(), n);

		}
		// normedSynonymTokens.entrySet().forEach(System.out::println);

		return normedSynonymTokens;
	}

	// private static Map<String, Set<String>> blowUpDictionary(Map<String,
	// Map<String, Integer>> synonymTokens) {
	// Map<String, Set<String>> blownUpDictionary = new HashMap<String,
	// Set<String>>();
	// for (Entry<Concept, Set<String>> dictEntry : dictionary.entrySet()) {
	//
	// final Concept diseaseID = dictEntry.getKey();
	//
	// blownUpDictionary.put(diseaseID, new HashSet<String>());
	//
	// /*
	// * Add normal
	// */
	// blownUpDictionary.get(diseaseID).addAll(dictEntry.getValue());
	//
	// /*
	// * Add blow up
	// */
	// for (String synonyms : dictEntry.getValue()) {
	//
	// String[] synTokens = synonyms.split(" ");
	//
	// if (synTokens.length == 1)
	// continue;
	//
	// for (int i = 0; i < synTokens.length; i++) {
	//
	// if (synonymTokens.containsKey(synTokens[i])) {
	// for (Entry<String, Integer> synToken :
	// synonymTokens.get(synTokens[i]).entrySet()) {
	//
	// if (synToken.getValue() <= 20) {
	// continue;
	// }
	//
	// final String blownUpEntry = buildBlowUpSyn(synTokens, i,
	// synToken.getKey());
	//
	// if (!includeDiseaseName(blownUpEntry)) {
	// continue;
	// }
	//
	// blownUpDictionary.get(diseaseID).add(blownUpEntry);
	// }
	// }
	// }
	// }
	// }
	// return blownUpDictionary;
	//
	// }

	// private static String buildBlowUpSyn(String[] synTokens, int i, String
	// synToken) {
	//
	// StringBuffer blownUpEntry = new StringBuffer();
	//
	// for (int j = 0; j < synTokens.length; j++) {
	// if (j != i) {
	// blownUpEntry.append(synTokens[j] + " ");
	// } else {
	// blownUpEntry.append(synToken + " ");
	// }
	// }
	//
	// return blownUpEntry.toString().trim();
	// }

	private static int extractExchangeableWordIndex(String[] indexArrayA, String[] indexArrayB) {

		for (int i = 0; i < indexArrayA.length; i++) {

			if (indexArrayA[i].equals(indexArrayB[i]))
				continue;
			else {
				return i;
			}
		}
		return -1;
	}

	private static String convertToIndexString(Map<String, Integer> tokenMapping, String syn) {
		StringBuffer indexString = new StringBuffer();

		String[] tokens = syn.split(" ");

		for (int i = 0; i < tokens.length; i++) {
			indexString.append(tokenMapping.get(tokens[i]) + " ");
		}

		return indexString.toString().trim();
	}

	private static String convertFromIndexString(Map<Integer, String> indexMapping, String indexString) {
		StringBuffer tokenString = new StringBuffer();

		String[] indexe = indexString.split(" ");

		for (int i = 0; i < indexe.length; i++) {
			tokenString.append(indexMapping.get(Integer.parseInt(indexe[i])) + " ");
		}

		return tokenString.toString().trim();
	}

	private static boolean includeDiseaseName(String n) {

		if (n.length() <= 3)
			return false;

		if (StringUtils.isUpperCase(n))
			return false;

		return true;
	}

}

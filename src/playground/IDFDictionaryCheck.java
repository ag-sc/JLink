package playground;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import medic.MEDICDisease;
import medic.dict.DiseaseDictionary;
import medic.dict.DiseaseDictionaryFactory;
import tfidf.TFIDF;
import tokenization.ETokenizationType;
import tokenization.StanfordLemmatizer;

public class IDFDictionaryCheck {

	private static final int N_GRAM_SIZE = 2;

	static private Map<String, Double> idf;

	static private Map<String, Double> termRatio;

	static private DiseaseDictionary dict;

	static private double maxIDF;
	static private StanfordLemmatizer lemmatizer;

	public static void main(String[] args) throws IOException {

		lemmatizer = new StanfordLemmatizer();

		dict = (DiseaseDictionary) restoreData("gen/medic_train_lemmatized_tokenized_dict.bin");

		if (dict == null) {
			dict = DiseaseDictionaryFactory.getInstance(true, ETokenizationType.SIMPLE, true, true, false, false);
			writeData("gen/medic_train_lemmatized_tokenized_dict.bin", dict);
		}

		File termRationFile = new File("gen/medline_bigram_term_ratio.csv");

		termRatio = readTermRatioFromFile(termRationFile);

		idf = (Map<String, Double>) restoreData("gen/bigram_dictionary_idf.sdhm");
		if (idf == null) {
			Map<String, List<String>> documents = new HashMap<>();

			for (Entry<MEDICDisease, Set<String>> dictEntry : dict.getDictionary().entrySet()) {

				documents.putIfAbsent(dictEntry.getKey().diseaseID, new ArrayList<>());

				documents.get(dictEntry.getKey().diseaseID)
						.addAll(getNTuples(dictEntry.getKey().normalizedForm, N_GRAM_SIZE));

				documents.get(dictEntry.getKey().diseaseID).addAll(dictEntry.getValue().stream()
						.flatMap(e -> getNTuples(e, N_GRAM_SIZE).stream()).collect(Collectors.toList()));

			}

			idf = TFIDF.getIDFs(documents);

			writeData("gen/bigram_dictionary_idf.sdhm", idf);
		}

		maxIDF = idf.values().stream().max(Double::compareTo).get();

		// getBestDictEntries("autosomal dominant multisystem
		// disorder").forEach(System.out::println);
		// getBestDictEntries("atrophic benign epidermolysis
		// bullosa").forEach(System.out::println);

	}

	/*
	 * token, nGrams
	 */
	private static Map<String, Set<String>> ngrams = new HashMap<>();

	private static Set<String> getNTuples(String token, int n) {

		if (ngrams.containsKey(token)) {
			return ngrams.get(token);
		}

		List<String> tokens = Arrays.asList(token.split(" "));

		Collections.sort(tokens);

		Set<String> ntuples = new HashSet<>();
		if (n >= 1)
			for (int j = 0; j < tokens.size(); j++) {
				final String t1 = tokens.get(j);
				ntuples.add(t1);
				if (n >= 2)
					for (int k = j + 1; k < tokens.size(); k++) {
						final String t2 = tokens.get(k);
						ntuples.add(t1 + " " + t2);
					}
			}
		ngrams.put(token, ntuples);

		return ntuples;

	}

	public static List<ResultTuple> getBestDictEntries(String diseaseMention) {

		diseaseMention = lemmatizer.lemmatizeDocument(diseaseMention);

		List<ResultTuple> results = new ArrayList<>();

		Set<String> diseaseMentions = getNTuples(diseaseMention, N_GRAM_SIZE);

		for (Entry<MEDICDisease, Set<String>> dictEntry : dict.getDictionary().entrySet()) {

			Set<String> disease = new HashSet<>();

			disease.addAll(getNTuples(dictEntry.getKey().normalizedForm, N_GRAM_SIZE));

			// double maxDiseaseScore = getScore(disease, disease);

			double score = getScore(diseaseMentions, disease);

			results.add(new ResultTuple(dictEntry.getKey().diseaseID, score));

			for (String synonym : dictEntry.getValue()) {
				disease = new HashSet<>();
				disease.addAll(getNTuples(synonym, N_GRAM_SIZE));

				score = getScore(diseaseMentions, disease);

				results.add(new ResultTuple(dictEntry.getKey().diseaseID, score));

			}
		}
		Collections.sort(results);

		return results;
	}

	private static Map<String, ResultTuple> resultCache = new HashMap<>();

	public static ResultTuple getBestDictEntry(final String diseaseMention) {

		if (resultCache.containsKey(diseaseMention))
			return resultCache.get(diseaseMention);

		double maxScore = 0;
		String bestDisease = "";

		Set<String> diseaseMentions = getNTuples(diseaseMention, N_GRAM_SIZE);
		for (Entry<MEDICDisease, Set<String>> dictEntry : dict.getDictionary().entrySet()) {

			Set<String> disease = new HashSet<>();

			disease.addAll(getNTuples(dictEntry.getKey().normalizedForm, N_GRAM_SIZE));

			double score = getScore(diseaseMentions, disease);

			if (score > maxScore) {
				maxScore = score;
				bestDisease = dictEntry.getKey().diseaseID;
			}

			for (String synonym : dictEntry.getValue()) {
				disease = new HashSet<>();
				disease.addAll(getNTuples(synonym, N_GRAM_SIZE));

				score = getScore(diseaseMentions, disease);

				if (score > maxScore) {
					maxScore = score;
					bestDisease = dictEntry.getKey().diseaseID;
				}
			}
		}

		ResultTuple result = new ResultTuple(bestDisease, maxScore);

		resultCache.put(diseaseMention, result);

		return result;
	}

	private static double getScore(Set<String> diseaseMention, Set<String> disease) {
		double score = 0;

		final Set<String> intersection = new HashSet<>(diseaseMention);
		intersection.retainAll(disease);

		final Set<String> distinct = new HashSet<>(diseaseMention);
		distinct.addAll(disease);
		distinct.removeAll(intersection);

		if (intersection.isEmpty())
			return -3;

		for (String sharedtoken : intersection) {
			score += (idf.getOrDefault(sharedtoken, 0d) / maxIDF) * termRatio.getOrDefault(sharedtoken, 1d);
		}
		// score /= (intersection.size() + distinct.size());

		// for (String missingtoken : distinct) {
		//
		// score -= idf.containsKey(missingtoken)
		// ? (idf.get(missingtoken) / maxIDF) *
		// termRatio.getOrDefault(missingtoken, 1d)
		// : termRatio.getOrDefault(missingtoken, 1d);
		// }

		// score += x / (intersection.size() + distinct.size());

		return score;

	}

	public static class ResultTuple implements Comparable<ResultTuple> {

		final public String ID;
		final public double score;

		public ResultTuple(String ID, double score) {
			this.ID = ID;
			this.score = score;
		}

		@Override
		public String toString() {
			return "ResultTuple [ID=" + ID + ", score=" + score + "]";
		}

		@Override
		public int compareTo(ResultTuple o) {
			return -Double.compare(score, o.score);
		}

	}

	private static Map<String, Double> readTermRatioFromFile(File file) throws IOException {
		System.out.print("Read medline term ratio...");
		Map<String, Double> termRation = new HashMap<>();
		Files.readAllLines(file.toPath()).stream().skip(1).forEach(line -> {

			final String[] data = line.split("\t");

			final String term = data[0];
			final double ratio = Double.parseDouble(data[3]);

			termRation.put(term, ratio);
		});
		System.out.println(" done!");
		return termRation;
	}

	private static void writeData(final String filename, final Object data) {
		final long t = System.currentTimeMillis();

		FileOutputStream fileOut;
		try {
			System.out.println("Write to filesystem...");
			fileOut = new FileOutputStream(filename);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(data);
			out.close();
			fileOut.close();
			System.out.println(
					"Serialized data is saved to : \"" + filename + "\" in " + (System.currentTimeMillis() - t));
		} catch (final Exception e) {
			System.out.println("Could not serialize data to: \"" + filename + "\": " + e.getMessage());
		}
	}

	static Object restoreData(final String filename) {
		final long t = System.currentTimeMillis();
		Object data = null;
		FileInputStream fileIn;
		System.out.println("Restore data from : \"" + filename + "\" ...");
		try {
			fileIn = new FileInputStream(filename);
			ObjectInputStream in;
			in = new ObjectInputStream(fileIn);
			data = in.readObject();
			in.close();
			fileIn.close();
			System.out.println(
					"Successfully restored data from : \"" + filename + "\" in " + (System.currentTimeMillis() - t));
		} catch (final Exception e) {
			System.out.println("Could not restored data from : \"" + filename + "\": " + e.getMessage());
			return null;
		}
		return data;
	}

}

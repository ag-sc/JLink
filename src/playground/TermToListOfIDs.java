package playground;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import medic.MEDICDisease;
import medic.dict.DiseaseDictionary;
import medic.dict.DiseaseDictionaryFactory;
import tokenization.ETokenizationType;

public class TermToListOfIDs {

	static private DiseaseDictionary dict;

	public static void main(String[] args) throws FileNotFoundException {
		/*
		 * Term, Set of IDs
		 */
		Map<String, Set<String>> termToList = new HashMap<>();

		dict = DiseaseDictionaryFactory.getInstance(true, ETokenizationType.SIMPLE, true, false, false, false);

		for (Entry<MEDICDisease, Set<String>> dictEntry : dict.getDictionary().entrySet()) {

			Set<String> terms = new HashSet<>();

			terms.addAll(getTerms(dictEntry.getKey().normalizedForm, true));

			for (String synonym : dictEntry.getValue()) {
				terms.addAll(getTerms(synonym, true));
			}

			final String ID = dictEntry.getKey().diseaseID;

			for (String term : terms) {
				if (term.length() > 1)
					termToList.putIfAbsent(term, new HashSet<>());
			}

			for (String term : terms) {
				if (term.length() > 1)
					termToList.get(term).add(ID);
			}

		}
		PrintStream ps = new PrintStream("train_bigram_lemmatized_term_id_list.csv");

		termToList.entrySet().stream().map(
				e -> (e.getKey() + "\t" + e.getValue().stream().map(s -> s + "\t").reduce("", String::concat)).trim())
				.forEach(ps::println);

		ps.close();
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

	public static List<String> getTerms(final String data, boolean bigrams) {

		final String[] d = data.split(" ");
		final List<String> terms = new ArrayList<>(Arrays.asList(d));

		if (bigrams) {
			for (int i = 0; i < d.length - 1; i++) {
				terms.add((d[i] + " " + d[i + 1]));
			}
		}
		return terms;
	}
}

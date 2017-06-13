package playground;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MedlineTermRation {

	public static void main(String[] args) throws IOException {

		File termMedlineFrequencyFile = new File("res/medline/bigram/termFrequency.csv");

		Map<String, Integer> termMedlineFrequency = readTermFrequencyFromFile(termMedlineFrequencyFile, null);

		File docMedlineFrequencyFile = new File("res/medline/bigram/docFrequency.csv");

		Map<String, Integer> docMedlineFrequency = readTermFrequencyFromFile(docMedlineFrequencyFile,
				termMedlineFrequency.keySet());

		PrintStream ps = new PrintStream("gen/medline_bigram_term_ratio.csv");

		for (String term : termMedlineFrequency.keySet()) {
			ps.println(term + "\t" + termMedlineFrequency.get(term) + "\t" + docMedlineFrequency.getOrDefault(term, 0));
		}

		ps.close();

	}

	private static Map<String, Integer> readTermFrequencyFromFile(File file, Set<String> filter) throws IOException {
		System.out.print("Read frequency...");
		Map<String, Integer> frequencies = new HashMap<>();
		int count = 0;

		BufferedReader br = new BufferedReader(new FileReader(file));

		String line = "";

		while ((line = br.readLine()) != null) {
			count++;
			final String[] data = line.split("\t");

			final String term = data[0];
			final Integer frequency = Double.valueOf(data[1]).intValue();

			if (filter == null) {
				frequencies.put(term, frequency);
			} else {
				if (filter.contains(term)) {
					frequencies.put(term, frequency);
				}
			}
			if (count % 100000 == 0)
				System.out.println("Count = " + count);
		}
		br.close();
		System.out.println(" done!");
		return frequencies;
	}

}

package playground;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CountDiseaseIDFrequencies {

	public static void main(String[] args) throws IOException {

		final String directoryTagsFileName = args[0];
		final String outputFile = args[1];

		File directoryTagsFile = new File(directoryTagsFileName);

		/*
		 * DiseaseID, Set of AbstractIDs
		 */
		Map<String, Integer> diseaseIDAbstractIDLists = countDiseaseIDFrequenciesFromDirectory(directoryTagsFile);

		diseaseIDAbstractIDLists.entrySet().stream().limit(2).forEach(System.out::println);
		System.out.println("Size = " + diseaseIDAbstractIDLists.size());
		System.out.println();

		PrintStream out = new PrintStream(outputFile);
		diseaseIDAbstractIDLists.entrySet().stream().map(e -> e.getKey() + "\t" + e.getValue()).forEach(out::println);
		out.close();

	}

	private static Map<String, Integer> countDiseaseIDFrequenciesFromDirectory(File directoryFile) throws IOException {
		System.out.println("Read disease id to abstarct id list... ");
		Map<String, Integer> diseaseIDFrequencies = new HashMap<>();

		List<File> filesInDir = Arrays.asList(directoryFile.listFiles());

		Collections.sort(filesInDir);

		final int numOfFiles = filesInDir.size();
		AtomicInteger progress = new AtomicInteger(0);
		for (File file : filesInDir) {

			if (!file.getName().endsWith("out"))
				continue;

			System.out.println("(" + progress.incrementAndGet() + "/" + numOfFiles + ") " + file.getName());

			Files.readAllLines(file.toPath()).stream().forEach(line -> {

				final String[] datas = line.split("=");

				final String[] data = datas[1].substring(1, datas[1].length() - 1).split(",");

				for (String diseaseID : data) {

					diseaseID = diseaseID.trim();

					diseaseIDFrequencies.put(diseaseID, diseaseIDFrequencies.getOrDefault(diseaseID, 0) + 1);
				}

			});
		}
		System.out.println(" done!");

		return diseaseIDFrequencies;
	}

}

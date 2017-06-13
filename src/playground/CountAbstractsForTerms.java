package playground;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CountAbstractsForTerms {

	public static void main(String[] args) throws IOException {

		final String termIDListFileName = args[0];
		final String directoryTermFileName = args[1];
		final String directoryTagsFileName = args[2];
		final String outputFile = args[3];

		File termIDListFile = new File(termIDListFileName);
		File directoryTagsFile = new File(directoryTagsFileName);
		File directoryTermFile = new File(directoryTermFileName);

		/*
		 * Term to diseaseIDs
		 */
		Map<String, Set<String>> termToDiseaseIDLists = readTermIDListFromFile(termIDListFile);

		termToDiseaseIDLists.entrySet().stream().limit(2).forEach(System.out::println);
		System.out.println("Size = " + termToDiseaseIDLists.size());
		System.out.println();

		/*
		 * Term to int
		 */
		Map<String, Integer> termIndexMapping = getTermIndexMapping(termToDiseaseIDLists);
		termIndexMapping.entrySet().stream().limit(2).forEach(System.out::println);
		System.out.println("Size = " + termIndexMapping.size());
		System.out.println();

		/*
		 * DiseaseID, Set of AbstractIDs
		 */
		Map<String, Set<Integer>> abstractToIDLists = readDiseaseIDToAbstractIDListFromDirectory(directoryTagsFile);

		abstractToIDLists.entrySet().stream().limit(2).forEach(System.out::println);
		System.out.println("Size = " + abstractToIDLists.size());
		System.out.println();

		/*
		 * AbstractID, Set of index for Terms
		 */
		Map<Integer, Set<Integer>> abstractToTermLists = readAbstractToTermListFromDirectory(directoryTermFile,
				termIndexMapping);

		abstractToTermLists.entrySet().stream().limit(2).forEach(System.out::println);
		System.out.println("Size = " + abstractToTermLists.size());
		System.out.println();

		/*
		 * term, numberOfAbstarcts that contains term and ID.
		 */
		Map<String, Integer> ret = new ConcurrentHashMap<>();

		final int maxTerms = termToDiseaseIDLists.size();

		System.out.println("Process terms...");
		AtomicInteger progress = new AtomicInteger(0);
		termToDiseaseIDLists.entrySet().parallelStream().forEach(termIDList -> {

			final Integer termIndex = termIndexMapping.get(termIDList.getKey());
			System.out.println("(" + progress.incrementAndGet() + "/" + maxTerms + ") " + termIDList.getKey());

			Set<Integer> correctAbstractIDs = new HashSet<>();

			for (String diseaseID : termIDList.getValue()) {

				if (abstractToIDLists.containsKey(diseaseID)) {
					for (Integer abstractID : abstractToIDLists.get(diseaseID)) {

						/*
						 * If abstract is tagged with ID AND abstract contains
						 * term: increase count.
						 */
						if (abstractToTermLists.containsKey(abstractID)) {
							if (abstractToTermLists.get(abstractID).contains(termIndex)) {
								correctAbstractIDs.add(abstractID);
							}
						}
					}
				}
			}
			System.out.println(termIDList.getKey() + "\t" + correctAbstractIDs.size());
			ret.put(termIDList.getKey(), correctAbstractIDs.size());

		});

		PrintStream out = new PrintStream(outputFile);
		ret.entrySet().stream().map(e -> e.getKey() + "\t" + e.getValue()).forEach(out::println);
		out.close();

	}

	private static Map<String, Integer> getTermIndexMapping(Map<String, Set<String>> termToIDLists) {
		System.out.println("Create index mapping for terms... ");

		Map<String, Integer> termIndexMapping = new HashMap<>();

		int indexCount = 0;
		for (String term : termToIDLists.keySet()) {

			termIndexMapping.put(term, indexCount);
			indexCount++;
		}
		return termIndexMapping;
	}

	private static Map<Integer, Set<Integer>> readAbstractToTermListFromDirectory(File directoryFile,
			Map<String, Integer> termIndexMapping) throws IOException {

		System.out.println("Read abstract to term list... ");
		Map<Integer, Set<Integer>> abstractToTermList = new HashMap<>();

		List<File> filesInDir = Arrays.asList(directoryFile.listFiles());

		Collections.sort(filesInDir);

		final int numOfFiles = filesInDir.size();
		AtomicInteger progress = new AtomicInteger(0);

		for (File file : filesInDir) {

			System.out.println("(" + progress.incrementAndGet() + "/" + numOfFiles + ") " + file.getName());
			Files.readAllLines(file.toPath()).stream().forEach(line -> {

				final String[] data = line.split("\t");

				final int medlineID = Integer.parseInt(data[0].trim());

				boolean isTerm = true;

				Set<Integer> validterms = new HashSet<>();

				/*
				 * skip first and collect valid terms
				 */
				for (String term : data) {
					isTerm = !isTerm;

					if (!isTerm)
						continue;

					if (!termIndexMapping.keySet().contains(term))
						continue;

					validterms.add(termIndexMapping.get(term));
				}

				if (!validterms.isEmpty()) {
					abstractToTermList.put(medlineID, validterms);
				}

			});
		}
		System.out.println(" done!");

		return abstractToTermList;

	}

	private static Map<String, Set<Integer>> readDiseaseIDToAbstractIDListFromDirectory(File directoryFile)
			throws IOException {
		System.out.println("Read disease id to abstarct id list... ");
		Map<String, Set<Integer>> diseaseIDToAbstractList = new HashMap<>(15000);

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

				final int medlineID = Integer.parseInt(datas[0].trim());

				final String[] data = datas[1].substring(1, datas[1].length() - 1).split(",");

				for (String diseaseID : data) {

					diseaseID = diseaseID.trim();

					diseaseIDToAbstractList.putIfAbsent(diseaseID, new HashSet<>());
					diseaseIDToAbstractList.get(diseaseID).add(medlineID);
				}

			});
		}
		System.out.println(" done!");

		return diseaseIDToAbstractList;
	}

	private static Map<String, Set<String>> readTermIDListFromFile(File file) throws IOException {
		System.out.print("Read term ID list...");
		Map<String, Set<String>> termToList = new HashMap<>();
		Files.readAllLines(file.toPath()).stream().forEach(line -> {

			final String[] data = line.split("\t");

			final String term = data[0].trim();

			if (!term.matches("(\\+|-)?\\d+")) {

				termToList.put(term, new HashSet<>());

				for (int ID = 1; ID < data.length; ID++) {
					termToList.get(term).add(data[ID].trim());
				}
			}
		});
		System.out.println(" done!");
		return termToList;
	}

}

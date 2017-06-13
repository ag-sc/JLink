package playground;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CountAbstractsForTermsBIGDATA {

	private static final String TUPLE_FILE_ENDING = ".tuple";

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
		 * DiseaseID, Set of AbstractIDs
		 */
		Map<String, Set<Integer>> diseaseIDAbstractIDLists = readDiseaseIDToAbstractIDListFromDirectory(
				directoryTagsFile);

		diseaseIDAbstractIDLists.entrySet().stream().limit(2).forEach(System.out::println);
		System.out.println("Size = " + diseaseIDAbstractIDLists.size());
		System.out.println();

		/*
		 * Writes tuples to file: AbstractID, Term
		 */
		writeAbstractTermTuple(directoryTermFile, termToDiseaseIDLists.keySet());

		System.out.println("Start counting... ");

		Map<String, Set<Integer>> termToAbstractIDSet = new HashMap<>();

		for (String possibleTerm : termToDiseaseIDLists.keySet()) {
			termToAbstractIDSet.put(possibleTerm, new HashSet<>());
		}

		List<File> filesInDir = Arrays.asList(directoryTermFile.listFiles()).stream()
				.filter(file -> file.getName().endsWith(TUPLE_FILE_ENDING)).collect(Collectors.toList());

		Collections.sort(filesInDir);

		final int numOfFiles = filesInDir.size();
		AtomicInteger progress = new AtomicInteger(0);
		for (File file : filesInDir) {

			System.out.println("(" + progress.incrementAndGet() + "/" + numOfFiles + ") " + file.getName());

			Files.readAllLines(file.toPath()).stream().forEach(line -> {

				final String[] data = line.split("\t");
				final Integer medlineID = Integer.parseInt(data[0]);
				final String term = data[1];

				for (String diseaseID : termToDiseaseIDLists.get(term)) {
					if (diseaseIDAbstractIDLists.containsKey(diseaseID)
							&& diseaseIDAbstractIDLists.get(diseaseID).contains(medlineID)) {
						termToAbstractIDSet.get(term).add(medlineID);
						break;
					}
				}

			});
		}
		System.out.println(" done!");

		PrintStream out = new PrintStream(outputFile);
		termToAbstractIDSet.entrySet().stream().map(e -> e.getKey() + "\t" + e.getValue().size()).forEach(out::println);
		out.close();

	}

	private static void writeAbstractTermTuple(File directoryFile, Set<String> possibleterms) throws IOException {

		System.out.println("Read abstract to term list... ");

		List<File> filesInDir = Arrays.asList(directoryFile.listFiles());

		Collections.sort(filesInDir);

		final int numOfFiles = filesInDir.size();
		AtomicInteger progress = new AtomicInteger(0);

		for (File file : filesInDir) {

			PrintStream psOut = new PrintStream(
					new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath() + TUPLE_FILE_ENDING)));

			System.out.println("(" + progress.incrementAndGet() + "/" + numOfFiles + ") " + file.getName());
			Files.readAllLines(file.toPath()).stream().forEach(line -> {

				final String[] data = line.split("\t");

				final int medlineID = Integer.parseInt(data[0].trim());

				boolean isTerm = true;

				/*
				 * skip first and collect valid terms
				 */
				for (String term : data) {
					isTerm = !isTerm;

					if (!isTerm)
						continue;

					if (!possibleterms.contains(term))
						continue;

					psOut.println(medlineID + "\t" + term);
				}

			});

			psOut.close();
		}
		System.out.println(" done!");

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

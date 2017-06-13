package playground;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import medic.dict.DiseaseDictionary;
import medic.dict.DiseaseDictionaryFactory;
import tokenization.ETokenizationType;
import tokenization.Tokenizer;
import util.StringUtil;

/**
 * This class contains a method that computes P. R. F1 given the training data,
 * develop data, medic as dictionary.
 * 
 * Thereto, a dictionary is build using the training annotations.
 * 
 * It simply iterates over all words in the tokenized text and matches the
 * longest dictionary entry first.
 * 
 * The result will give a upper bound for BIRE that can be achieved using only
 * direct match features.
 * 
 * @author hterhors
 * 
 *         @formatter:off
 * 
 *         Results:
 *
 *          PFR1 without any Conjunctions / Disjunctions (Both count as False negative): 
 *          tp = 251.0
 *          fp = 45.0
 *          fn = 85.0 
 *         
 *          Micro precision = 0.848 
 *          Micro recall = 0.747 
 *          Micro F1 = 0.794
 *          
 *          Macro precision = 0.868 
 *          Macro recall = 0.796 
 *          Macro F1 = 0.814
 *
 *          PFR1 without any Conjunctions / Disjunctions (Both count as False negative). Not allowing Alternate ID mapping: 
 *			tp = 241.0
 *			fp = 55.0
 *			fn = 95.0
 *			Micro precision = 0.814
 *			Micro recall = 0.717
 *			Micro F1 = 0.763
 *			
 *			Macro precision = 0.833
 *			Macro recall = 0.762
 *			Macro F1 = 0.78
 *
 *
 *          
 *          
 *          
 *          PFR1Extended: Allowing conjunctions. Disjunctions does count as False negative: 
 *			tp = 253.0
 *			fp = 37.0
 *			fn = 77.0
 *
 *			Micro precision = 0.872
 *			Micro recall = 0.767
 *			Micro F1 = 0.816
 *			
 *			Macro precision = 0.872
 *			Macro recall = 0.809
 *			Macro F1 = 0.824
 *			
 *
 *          PFR1Extended: Allowing conjunctions. Disjunctions does count as False negative. Not allowing Alternate ID mapping: 
 *			tp = 243.0
 *			fp = 48.0
 *			fn = 88.0
 *			Micro precision = 0.835
 *			Micro recall = 0.734
 *			Micro F1 = 0.781
 *			
 *			Macro precision = 0.837
 *			Macro recall = 0.775
 *			Macro F1 = 0.789
 *
 *
 *
 *
 *         @formatter:on
 * 
 *         Jan 20, 2016
 */
public class DirectEntryMatchAndDisambiguationStream {

	public static void main(String[] args) throws Exception {

		new DirectEntryMatchAndDisambiguationStream(
				args.length != 0 ? args : new String[] { "gen/MedicTrainDevModel.model", "7" });

	}

	/**
	 * The disease dictionary build from medic extended by train and develop
	 * data.
	 */
	public DiseaseDictionary dict;

	/**
	 * Dictionary build from the disease dictionary entries.
	 */
	private List<String> dictionary;

	public DirectEntryMatchAndDisambiguationStream(String[] args) throws IOException {
		final String modelName = args[0];
		System.out.println("Model name = " + modelName);

		dict = (DiseaseDictionary) restoreData(modelName);

		if (dict == null) {
			System.out.println("Cannot load model. Read Medic, Train and Develop Data...");
			dict = DiseaseDictionaryFactory.getInstance(true, ETokenizationType.SIMPLE, true, true, false, false);
			writeData(modelName, dict);
		}

		final String directoryName = "/home/hterhors//MasterArbeit/source/data/MEDLINE/asciiFiles/" + args[1];

		final String outputFileName = "gen/disease_recognition_full_medline_" + args[1];
		// final String directoryName =
		// "/home/hterhors/MasterArbeit/source/data/MEDLINE/asciiFiles/" +
		// args[1];
		//
		// final String outputFileName = "gen/disease_recognition_full_medline_"
		// + args[1];

		dictionary = new ArrayList<String>();

		System.out.println("Build dictionary from model...");
		generateDictionary();

		System.out.println("Sort dictionary entries...");
		sortDicitonary();

		System.out.println("Search for data in " + directoryName);
		final File dir = new File(directoryName);
		if (!dir.isDirectory()) {
			System.out.println("Can not find any data. Exit system!");
			System.exit(1);
		}

		PrintStream outPut = new PrintStream(outputFileName);
		final File[] directoryListing = dir.listFiles();

		System.out.println("Start processing " + directoryListing.length + " files...");
		int fileCounter = 0;
		final int numOfFiles = directoryListing.length;
		long totalTime = System.currentTimeMillis();

		for (File file : directoryListing) {
			Set<Document> documents = new HashSet<Document>();
			long docTime = System.currentTimeMillis();
			fileCounter++;
			System.out.println("Read file " + fileCounter + "...");
			BufferedReader br = new BufferedReader(new FileReader(file));

			String line = "";
			while ((line = br.readLine()) != null) {

				final String document[] = line.split("\t", 2);
				documents.add(new Document(document[0], Tokenizer.getTokenizedForm(document[1])));

			}
			br.close();

			System.out.println("Start parallel processing of " + documents.size() + " documents...");
			/*
			 * This map stores for each document (Key) a list of disease surface
			 * forms (Value) that could be found in the tokenized text.
			 */
			final Map<String, Set<String>> findings = new ConcurrentHashMap<String, Set<String>>();
			final AtomicInteger docCounter = new AtomicInteger(0);

			PrintStream ps = new PrintStream(new File("gen/disease.out"));

			documents.parallelStream().forEach(doc -> {
				findAnnotations(doc, findings, docCounter.incrementAndGet(), documents.size());

				if (findings.size() == 50)
					synchronized (ps) {
						findings.entrySet().stream().forEach(ps::println);
						findings.clear();
					}

			});
			ps.close();
			findings.entrySet().stream().forEach(outPut::println);

			System.out.println("Write findings to file...");

			System.out.println(
					"Time needed to process all documents in file = " + (System.currentTimeMillis() - docTime));
			System.out.println("Expected remaining time to finish all files = "
					+ ((System.currentTimeMillis() - totalTime) / fileCounter) * (numOfFiles - fileCounter));
		}

		outPut.close();
	}

	private void findAnnotations(Document document, Map<String, Set<String>> findings, int docCounter,
			final int maxDocuments) {
		System.out.println("Process file " + docCounter + "/" + maxDocuments + " ...");
		/*
		 * Find annotations
		 * 
		 * Remove findings from the text.
		 */

		/*
		 * Search pattern for disease surface forms in tokenized text.
		 */
		Pattern p;

		/*
		 * Patterns matcher.
		 */
		Matcher m;

		/*
		 * Found match from the pattern provided by the matcher.
		 */
		String match;
		/*
		 * The text from the tokenized document.
		 */
		String text;

		/*
		 * The finding converted into diseaseID using the dictionary.
		 */
		String diseaseID;

		/*
		 * The found disease surface form.
		 */
		String diseaseSurfaceForm;

		text = document.tokenizedDocument;
		findings.put(document.documentID, new HashSet<String>());
		for (String dictEntry : dictionary) {

			p = Pattern.compile("((^| )" + dictEntry + "( |$)){1}");
			m = p.matcher(text);

			if (m.find()) {

				match = m.group(1);

				text = text.replaceAll(match.trim() + "( |$)", "");
				/*-
				 * 1) Convert match to ID:
				 * 
				 * 2) Convert ID to preferred ID if ID is an alternate ID.
				 * 
				 * 3) Add match to findings.
				 */
				diseaseSurfaceForm = match.trim();

				diseaseID = dict.getIDBySurfaceForm(diseaseSurfaceForm);

				if (diseaseID == null) {
					System.err.println("NULL ID DETECTED!!! " + diseaseSurfaceForm);
				} else {
					diseaseID = dict.mapToPrefIDIfAny(diseaseID);
				}

				/*
				 * Break if there
				 */
				if (diseaseID == null) {
					System.out.println("Unkwon disease ID for surface form = " + diseaseSurfaceForm);
					break;
				}

				findings.get(document.documentID).add(diseaseID);
			}
		}
	}

	private void sortDicitonary() {
		/*
		 * Sort list by length to take max lenght match first.
		 */
		Collections.sort(dictionary, (o1, o2) -> -Integer.compare(o1.length(), o2.length()));
	}

	private void generateDictionary() {

		/*
		 * Dictionary preferred entry;
		 */

		for (String dictEntry : dict.surfaceFormToDiseaseID.keySet()) {

			if (applyFilter(dictEntry))
				if (!dictionary.contains(dictEntry))
					dictionary.add(dictEntry);

		}

		// dictionary.forEach(System.out::println);

	}

	private static boolean applyFilter(String n) {

		if (n.length() <= 3)
			return false;

		if (StringUtil.isUpperCase(n))
			return false;

		if (n.equals("disease"))
			return false;

		if (n.equals("syndrome"))
			return false;

		if (n.equals("diseases"))
			return false;

		if (n.equals("syndromes"))
			return false;

		return true;
	}

	public void writeData(final String filename, final Object data) {
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

	public static Object restoreData(final String filename) {
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

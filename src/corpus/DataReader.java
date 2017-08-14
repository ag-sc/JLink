package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import main.JLink;
import main.setting.ECorpus;
import main.setting.EDataset;
import tokenization.Tokenizer;

public class DataReader {

	private static final int INDEX_DISEASE_SURFACEFORM = 3;
	private static final int INDEX_DISEASE_ID = 5;
	private static final int INDEX_DISEASE_OR_CHEMICAL = 4;
	private static final int INDEX_DOC_ID = 0;
	private static final String SEPERATOR = "\t";

	private static String getInputFileName(final EDataset dataset) {
		String inputFileName = null;

		String prefix = DatasetConfig.PROPERTIES.getProperty("DATASET_PREFIX");

		switch (JLink.setting.corpus) {
		case BC5:
			// inputFileName = prefix + "/bc5/" + "/CDR_" + dataset.fullName +
			// "_corpus.txt";
			if (dataset == EDataset.TRAIN) {
				inputFileName = DatasetConfig.getTrainCorpusPath();
			} else if (dataset == EDataset.DEVELOP) {
				inputFileName = DatasetConfig.getDevelopmentCorpusPath();
			} else if (dataset == EDataset.TEST) {
				inputFileName = DatasetConfig.getTestCorpusPath();
			}
			break;
		case NCBI:
			inputFileName = prefix + "/ncbi_disease/" + "/NCBI_" + dataset.fullName + "_corpus.txt";
			break;

		default:
			break;
		}
		System.out.println("inputFileName = " + inputFileName);
		return inputFileName;
	}

	public static Map<String, String> loadTexts(final EDataset dataset) throws IOException {

		HashMap<String, String> texts = new HashMap<String, String>();

		final String inputFileName;

		inputFileName = getInputFileName(dataset);

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));

		String line = "";

		StringBuffer abstarctText = new StringBuffer();

		String pubmedID = null;
		while ((line = br.readLine()) != null) {
			if (line.trim().isEmpty() || line.startsWith("#"))
				continue;

			/*
			 * Filter abstract lines and store them.
			 */
			if (line.matches("[0-9]+\\|[ta]\\|.*")) {
				abstarctText.append(line.split("\\|")[2]);
				abstarctText.append(" ");
				pubmedID = line.split("\\|")[0];
				continue;
			}
			if (abstarctText.length() > 0) {
				texts.put(pubmedID, abstarctText.toString());
				abstarctText = new StringBuffer();
			}
		}
		br.close();
		return texts;
	}

	public static Map<String, Set<String>> getDiseaseSurfaceFormToIDMapping(final EDataset dataset) throws IOException {

		Map<String, Set<String>> ids = new HashMap<String, Set<String>>();
		final String inputFileName;

		inputFileName = getInputFileName(dataset);

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));
		ECorpus corpus = JLink.setting.corpus;

		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.trim().isEmpty() || line.startsWith("#"))
				continue;

			/*
			 * Filter abstract lines and store them.
			 */
			if (line.matches("[0-9]+\\|[ta]\\|.*")) {
				continue;
			} else {
				final String[] data = line.split(SEPERATOR);

				if (data.length < 6)
					continue;

				String id = data[INDEX_DISEASE_ID].trim();
				String sf = data[INDEX_DISEASE_SURFACEFORM].trim();

				if ((corpus == ECorpus.BC5) && data[INDEX_DISEASE_OR_CHEMICAL].trim().equals("Chemical"))
					continue;

				if ((corpus == ECorpus.BC5) && id.equals("MESH:-1"))
					continue;
				/*
				 * Split id at '+'-sign and assign both ids in separate to the
				 * specific disease surface form.
				 */
				for (String partID : id.split("\\+")) {
					partID = buildDiseaseID(partID);

					ids.putIfAbsent(partID, new HashSet<String>());

					ids.get(partID).add(sf);
				}
			}
		}
		br.close();
		return ids;
	}

	public static Map<String, Set<String>> getChemicalSurfaceFormToIDMapping(final EDataset dataset)
			throws IOException {

		Map<String, Set<String>> ids = new HashMap<String, Set<String>>();
		final String inputFileName;

		inputFileName = getInputFileName(dataset);

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));
		ECorpus corpus = JLink.setting.corpus;

		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.trim().isEmpty() || line.startsWith("#"))
				continue;

			/*
			 * Filter abstract lines and store them.
			 */
			if (line.matches("[0-9]+\\|[ta]\\|.*")) {
				continue;
			} else {
				final String[] data = line.split(SEPERATOR);

				if (data.length < 6)
					continue;

				String id = data[INDEX_DISEASE_ID].trim();
				String sf = data[INDEX_DISEASE_SURFACEFORM].trim();

				if ((corpus == ECorpus.BC5) && !data[INDEX_DISEASE_OR_CHEMICAL].trim().equals("Chemical"))
					continue;

				if ((corpus == ECorpus.BC5) && id.equals("MESH:-1"))
					continue;
				/*
				 * Split id at '+'-sign and assign both ids in separate to the
				 * specific disease surface form.
				 */
				for (String partID : id.split("\\+")) {
					partID = buildDiseaseID(partID);

					ids.putIfAbsent(partID, new HashSet<String>());

					ids.get(partID).add(sf);
				}
			}
		}
		br.close();
		return ids;
	}

	public static Map<String, Set<String>> getDiseaseChemicalSurfaceFormToIDMapping(final EDataset dataset)
			throws IOException {

		Map<String, Set<String>> ids = new HashMap<String, Set<String>>();
		final String inputFileName;

		inputFileName = getInputFileName(dataset);

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));
		ECorpus corpus = JLink.setting.corpus;

		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.trim().isEmpty() || line.startsWith("#"))
				continue;

			/*
			 * Filter abstract lines and store them.
			 */
			if (line.matches("[0-9]+\\|[ta]\\|.*")) {
				continue;
			} else {
				final String[] data = line.split(SEPERATOR);

				if (data.length < 6)
					continue;

				String id = data[INDEX_DISEASE_ID].trim();
				String sf = data[INDEX_DISEASE_SURFACEFORM].trim();

				if ((corpus == ECorpus.BC5) && id.equals("MESH:-1"))
					continue;
				/*
				 * Split id at '+'-sign and assign both ids in separate to the
				 * specific disease surface form.
				 */
				for (String partID : id.split("\\+")) {
					partID = buildDiseaseID(partID);

					ids.putIfAbsent(partID, new HashSet<String>());

					ids.get(partID).add(sf);
				}
			}
		}
		br.close();
		return ids;
	}

	public static Set<String> getAllDiseaseIDs(final EDataset dataset) throws IOException {

		Set<String> ids = new HashSet<String>();

		final String inputFileName;

		inputFileName = getInputFileName(dataset);

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));
		ECorpus corpus = JLink.setting.corpus;

		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.trim().isEmpty() || line.startsWith("#"))
				continue;

			/*
			 * Filter abstract lines and store them.
			 */
			if (line.matches("[0-9]+\\|[ta]\\|.*")) {
				continue;
			} else {
				String id = line.split(SEPERATOR)[INDEX_DISEASE_ID].trim();

				if ((corpus == ECorpus.BC5)
						&& line.split(SEPERATOR)[INDEX_DISEASE_OR_CHEMICAL].trim().equals("Chemical"))
					continue;

				if (line.split(SEPERATOR).length < 6)
					continue;

				// if (id.startsWith("OMIM:") || id.startsWith("MESH:"))
				// ids.add(id);
				// else
				// ids.add("MESH:" + id);
				id = buildDiseaseID(id);
				ids.add(id);

			}
		}
		br.close();
		return ids;
	}

	final private static int MEDIC_INDEX_DISEASE_NAME = 0;
	final private static int MEDIC_INDEX_DISEASE_MAIN_ID = 1;
	final private static int MEDIC_INDEX_DISEASE_SYNONYMS = 7;

	private static final int MEDIC_INDEX_ALTERNATE_IDS = 2;

	private static Map<String, Map<String, Integer>> countMEDICSurface2ID() throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(DatasetConfig.PROPERTIES.getProperty("MEDIC"))));

		String line = "";
		Map<String, Map<String, Integer>> ids = new HashMap<>();

		while ((line = br.readLine()) != null) {
			if (line.trim().isEmpty() || line.startsWith("#"))
				continue;

			String[] data = line.split(SEPERATOR);

			if (data.length < 8)
				continue;

			final String normalizedForm = Tokenizer.getTokenizedForm(data[MEDIC_INDEX_DISEASE_NAME]);
			final String diseaseID = data[MEDIC_INDEX_DISEASE_MAIN_ID];

			ids.putIfAbsent(normalizedForm, new HashMap<>());

			ids.get(normalizedForm).put(diseaseID, 1 + ids.get(normalizedForm).getOrDefault(diseaseID, 0));

			final Set<String> synonyms = new HashSet<String>(Arrays
					.asList(data[MEDIC_INDEX_DISEASE_SYNONYMS].split("\\|")).stream().filter(d -> !d.trim().isEmpty())
					.map(s -> Tokenizer.getTokenizedForm(s)).collect(Collectors.toSet()));

			for (String syn : synonyms) {
				ids.putIfAbsent(syn, new HashMap<>());

				ids.get(syn).put(diseaseID, 1 + ids.get(syn).getOrDefault(diseaseID, 0));
			}

		}
		br.close();
		return ids;
	}

	/**
	 * Returns the annotation data for the given dataset.
	 *
	 * 
	 * 
	 * @param DATA_SET
	 * @return Map(DocumentID, HashSet(DiseaseSurfaceForm))
	 * @throws IOException
	 */
	public static Map<String, Set<String>> loadDiseaseAnnotationData(final EDataset dataset, boolean getDiseaseIDs,
			boolean conv2PrefID) throws IOException {

		Map<String, Set<String>> goldData = new HashMap<String, Set<String>>();
		final String inputFileName;

		inputFileName = getInputFileName(dataset);
		ECorpus corpus = JLink.setting.corpus;

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));

		String line = "";

		Set<String> diseases = new HashSet<String>();
		String docID = null;
		String disease = null;
		while ((line = br.readLine()) != null) {

			final String data[];
			/*
			 * Filter empty lines.
			 */
			if (line.trim().isEmpty()) {
				goldData.put(docID, diseases);
				diseases = new HashSet<String>();
				continue;
			}

			data = line.split(SEPERATOR);

			/*
			 * Filter abstract lines and store them.
			 */
			if (line.matches("[0-9]+\\|[ta]\\|.*")) {
			} else {
				/*
				 * Filter wrong lines.
				 */
				if (data.length < 6) {
					continue;
				}

				if ((corpus == ECorpus.BC5)
						&& line.split(SEPERATOR)[INDEX_DISEASE_OR_CHEMICAL].trim().equals("Chemical"))
					continue;

				/*
				 * Get required data.
				 */
				disease = data[getDiseaseIDs ? INDEX_DISEASE_ID : INDEX_DISEASE_SURFACEFORM].trim();

				if (disease.equals("MESH:-1"))
					continue;

				docID = data[INDEX_DOC_ID].trim();

				if (disease.equals("-1"))
					continue;

				if (getDiseaseIDs) {
					disease = buildDiseaseID(disease);
				}

				diseases.add(disease);
			}
		}
		goldData.put(docID, diseases);
		br.close();
		return goldData;
	}

	/**
	 * Returns the annotation data for the given dataset.
	 *
	 * 
	 * 
	 * @param DATA_SET
	 * @return Map(DocumentID, HashSet(chemicalSurfaceForm))
	 * @throws IOException
	 */
	public static Map<String, Set<String>> loadChemicalAnnotationData(final EDataset dataset, boolean getDiseaseIDs,
			boolean conv2PrefID) throws IOException {

		Map<String, Set<String>> goldData = new HashMap<String, Set<String>>();
		final String inputFileName;

		inputFileName = getInputFileName(dataset);
		ECorpus corpus = JLink.setting.corpus;

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));

		String line = "";

		Set<String> diseases = new HashSet<String>();
		String docID = null;
		String disease = null;
		while ((line = br.readLine()) != null) {

			final String data[];
			/*
			 * Filter empty lines.
			 */
			if (line.trim().isEmpty()) {
				goldData.put(docID, diseases);
				diseases = new HashSet<String>();
				continue;
			}

			data = line.split(SEPERATOR);

			/*
			 * Filter abstract lines and store them.
			 */
			if (line.matches("[0-9]+\\|[ta]\\|.*")) {
			} else {
				/*
				 * Filter wrong lines.
				 */
				if (data.length < 6) {
					continue;
				}

				if ((corpus == ECorpus.BC5)
						&& !line.split(SEPERATOR)[INDEX_DISEASE_OR_CHEMICAL].trim().equals("Chemical"))
					continue;

				/*
				 * Get required data.
				 */
				disease = data[getDiseaseIDs ? INDEX_DISEASE_ID : INDEX_DISEASE_SURFACEFORM].trim();
				docID = data[INDEX_DOC_ID].trim().replaceAll("\"", ""); // remove
																		// quotes

				// if (disease.equals("-1"))
				// continue;

				if (getDiseaseIDs) {
					disease = buildDiseaseID(disease);
				}

				diseases.add(disease);
			}
		}
		goldData.put(docID, diseases);
		br.close();
		return goldData;
	}

	/**
	 * Returns the annotation data for the given dataset.
	 *
	 * 
	 * 
	 * @param DATA_SET
	 * @return Map(DocumentID, HashSet(chemicalSurfaceForm))
	 * @throws IOException
	 */
	public static Map<String, Set<String>> loadDiseaseChemicalAnnotationData(final EDataset dataset,
			boolean getDiseaseIDs, boolean conv2PrefID) throws IOException {

		Map<String, Set<String>> goldData = new HashMap<String, Set<String>>();
		final String inputFileName;

		inputFileName = getInputFileName(dataset);
		ECorpus corpus = JLink.setting.corpus;

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));

		String line = "";

		Set<String> diseases = new HashSet<String>();
		String docID = null;
		String disease = null;
		while ((line = br.readLine()) != null) {

			final String data[];
			/*
			 * Filter empty lines.
			 */
			if (line.trim().isEmpty()) {
				goldData.put(docID, diseases);
				diseases = new HashSet<String>();
				continue;
			}

			data = line.split(SEPERATOR);

			/*
			 * Filter abstract lines and store them.
			 */
			if (line.matches("[0-9]+\\|[ta]\\|.*")) {
			} else {
				/*
				 * Filter wrong lines.
				 */
				if (data.length < 6) {
					continue;
				}

				/*
				 * Get required data.
				 */
				disease = data[getDiseaseIDs ? INDEX_DISEASE_ID : INDEX_DISEASE_SURFACEFORM].trim();
				docID = data[INDEX_DOC_ID].trim().replaceAll("\"", ""); // remove
				// quotes

				// if (disease.equals("-1"))
				// continue;

				if (getDiseaseIDs) {
					disease = buildDiseaseID(disease);
				}

				diseases.add(disease);
			}
		}
		goldData.put(docID, diseases);
		br.close();
		return goldData;
	}

	public static String buildDiseaseID(String disease) {
		if (!(disease.contains("|") || disease.contains("+"))) {
			if (disease.startsWith("OMIM:") || disease.startsWith("MESH:")) {
			} else {
				disease = "MESH:" + disease.trim();
			}
		} else {
			String[] s_diseaseIDs = disease.split("\\||\\+");
			List<String> diseaseIDList = new ArrayList<String>();

			for (String s_diseaseID : s_diseaseIDs) {
				if (s_diseaseID.startsWith("OMIM:") || s_diseaseID.startsWith("MESH:")) {
					diseaseIDList.add(s_diseaseID);
				} else {
					diseaseIDList.add("MESH:" + s_diseaseID);
				}
			}
			StringBuffer newDiseaseID = new StringBuffer();

			Collections.sort(diseaseIDList);

			for (int i = 0; i < diseaseIDList.size(); i++) {
				newDiseaseID.append(diseaseIDList.get(i));
				if (i < diseaseIDList.size() - 1) {
					if (disease.contains("|"))
						newDiseaseID.append("|");
					if (disease.contains("+"))
						newDiseaseID.append("+");
				}
			}

			disease = newDiseaseID.toString();
		}
		return disease;
	}
}

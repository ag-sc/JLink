package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.Concept;
import main.EType;
import main.JLink;
import variables.EEntityType;
import variables.EntityAnnotation;
import variables.JLinkState;
import variables.LabeledJlinkDocument;

public class CorpusLoader {
	private static Logger log = LogManager.getFormatterLogger();

	public static void main(String[] args) {

		DocumentCorpus corpus = loadTestCorpus();

		corpus.getDocuments().forEach(System.out::println);
	}

	public static DocumentCorpus loadTrainCorpus() {
		return loadCorpus(DatasetConfig.getTrainCorpusPath());
	}

	public static DocumentCorpus loadTestCorpus() {
		return loadCorpus(DatasetConfig.getTestCorpusPath());
	}

	public static DocumentCorpus loadDevelopCorpus() {
		return loadCorpus(DatasetConfig.getDevelopmentCorpusPath());
	}

	private static DocumentCorpus loadCorpus(String corpusPath) {
		try {
			File textualCorpus = new File(corpusPath);

			if (!textualCorpus.exists()) {
				log.error("Could not find corpus: " + corpusPath);
				System.exit(1);
			}

			return convertDatasetToJavaBinaries(textualCorpus, JLink.type);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static DocumentCorpus convertDatasetToJavaBinaries(File textualCorpus, EType type) throws IOException {

		DocumentCorpus corpus = new DocumentCorpus();

		Set<CorpusAnnotation> annotations = loadAnnotations(textualCorpus);

		for (Entry<String, String> textualDoc : loadTexts(textualCorpus).entrySet()) {

			String documentID = textualDoc.getKey();

			String documentContent = textualDoc.getValue();

			Tokenization tokenization = new SimpleRegexTokenizer().tokenize(documentContent);

			LabeledJlinkDocument doc = new LabeledJlinkDocument(documentID, tokenization.originalSentence,
					tokenization.tokens);

			JLinkState goldState = new JLinkState(doc);

			for (CorpusAnnotation corpusAnnotation : annotations) {

				if (type == EType.Disease_Chemical) {
				} else if (type == EType.Chemical && corpusAnnotation.entityType == EEntityType.CHEMICAL) {
				} else if (type == EType.Disease && corpusAnnotation.entityType == EEntityType.DISEASE) {
				} else {
					continue;
				}

				if (!corpusAnnotation.documentID.equals(documentID))
					continue;

				int fromTokenIndex = findTokenForPosition(corpusAnnotation.start, tokenization, true);
				int toTokenIndex = findTokenForPosition(corpusAnnotation.end, tokenization, false) + 1;

				Concept concept = new Concept(corpusAnnotation.conceptID, EEntityType.UNK);

				EntityAnnotation entity = new EntityAnnotation(goldState, concept, corpusAnnotation.surfaceForm,
						fromTokenIndex, toTokenIndex, corpusAnnotation.start, corpusAnnotation.end);
				goldState.addEntity(entity);

			}
			doc.setGoldResult(goldState);
			corpus.addDocument(doc);
		}

		return corpus;
	}

	private static Map<String, String> loadTexts(File inputFileName) throws IOException {

		HashMap<String, String> texts = new HashMap<String, String>();

		BufferedReader br = new BufferedReader(new FileReader(inputFileName));

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

	private static int findTokenForPosition(int documentLevelCharacterPosition, Tokenization tokenization,
			boolean findLowerBound) {
		int sentenceLevelCharacterPosition = documentLevelCharacterPosition - tokenization.absoluteStartOffset;
		return ParsingUtils.binarySpanSearch(tokenization.tokens, sentenceLevelCharacterPosition, findLowerBound);
	}

	private static final int INDEX_DISEASE_SURFACEFORM = 3;
	private static final int INDEX_DISEASE_ID = 5;
	private static final int INDEX_DISEASE_OR_CHEMICAL = 4;
	private static final int INDEX_DOC_ID = 0;
	private static final String SEPERATOR = "\t";

	public static Set<CorpusAnnotation> loadAnnotations(File textualCorpus) throws IOException {

		Set<CorpusAnnotation> goldData = new HashSet<>();

		BufferedReader br = new BufferedReader(new FileReader(textualCorpus));

		String line = "";

		String conceptID;
		int start;
		int end;
		String surfaceForm;
		String documentID;
		EEntityType entityType;

		while ((line = br.readLine()) != null) {

			final String data[];
			/*
			 * Filter empty lines.
			 */
			if (line.trim().isEmpty()) {
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

				conceptID = DataReader.buildDiseaseID(data[INDEX_DISEASE_ID].trim());

				if (conceptID.equals("-1"))
					continue;

				entityType = line.split(SEPERATOR)[INDEX_DISEASE_OR_CHEMICAL].trim().equals("Chemical")
						? EEntityType.CHEMICAL : EEntityType.DISEASE;

				surfaceForm = data[INDEX_DISEASE_SURFACEFORM].trim();
				documentID = data[INDEX_DOC_ID].trim();
				start = Integer.parseInt(data[1].trim());
				end = Integer.parseInt(data[2].trim());

				goldData.add(new CorpusAnnotation(conceptID, start, end, surfaceForm, documentID, entityType));
			}
		}
		br.close();
		return goldData;
	}
}

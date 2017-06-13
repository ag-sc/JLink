package dictionary.subdicts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import corpus.DatasetConfig;
import dictionary.Concept;
import dictionary.DictionaryEntry;
import dictionary.EOrigin;
import tokenization.Tokenizer;
import util.StringUtils;
import variables.EEntityType;

public class OMIMDictionary implements ISubDictionary {

	public static void main(String[] args) {
		OMIMDictionary dict = new OMIMDictionary(2);

		dict.build();

		dict.dictionary.entrySet().forEach(System.out::println);
	}

	private static final String OMIM_FILE = DatasetConfig.PROPERTIES.getProperty("OMIM");
	private static final String CHECK_IF_PHENO_TYPE_ID = "\\([1-4]\\)";
	private static final int OMIM_ID_LENGHT = 6;
	private static final String CHECK_IF_ID = "[0-9]{" + OMIM_ID_LENGHT + "}";
	final private Map<String, Set<Concept>> sortedSurfaceFormDict = new HashMap<>();

	private int priority = 1;
	final private Map<Concept, Set<String>> conceptBasedDictionary = new HashMap<>();

	public OMIMDictionary(final int priority) {
		this.priority = priority;
	}

	final Map<DictionaryEntry, Set<Concept>> dictionary = new HashMap<>();
	final Map<String, Set<Concept>> surfaceFormDict = new HashMap<>();
	final Map<String, Set<Concept>> noVovalsSurfaceFormDict = new HashMap<>();

	final Set<String> tokensIndDict = new HashSet<>();

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean containsEntry(DictionaryEntry entry) {
		return dictionary.containsKey(entry);
	}

	@Override
	public Map<DictionaryEntry, Set<Concept>> getDictionary() {
		return dictionary;
	}

	@Override
	public void setPriority(final int priority) {
		this.priority = priority;
	}

	@Override
	public void build() {
		try {
			/*
			 * Extend dictionary to OMIM knowledge
			 */
			try {

				final BufferedReader br = new BufferedReader(new FileReader(OMIM_FILE));
				String line = br.readLine();
				while ((line = br.readLine()) != null) {
					/*
					 * Ignore special cases...
					 */
					if (line.startsWith("#") || line.startsWith("[") || line.startsWith("?")
					// || line.startsWith("{")
							|| line.trim().isEmpty()) {
						continue;
					}
					final String[] preData = line.split("\\|", 2)[0].split(" ");
					for (int i = 0; i < preData.length; i++) {
						preData[i] = preData[i].trim();
					}
					final String[] data = new String[] { "", "", "" };

					final int lastIndex = preData.length - 1;
					if (preData[lastIndex].trim().matches(CHECK_IF_PHENO_TYPE_ID)) {
						final int lastDiseaseIndex;
						if (preData[lastIndex - 1].trim().matches(CHECK_IF_ID)) {
							data[1] = preData[lastIndex - 1].trim();
							lastDiseaseIndex = lastIndex - 1;
						} else {
							data[1] = null;
							lastDiseaseIndex = lastIndex;
						}
						for (int i = 0; i < lastDiseaseIndex; i++) {
							data[0] += preData[i] + " ";
						}
						data[2] = preData[lastIndex].substring(1, 2);
					} else if (preData[lastIndex].matches(CHECK_IF_ID)) {
						for (int i = 0; i < lastIndex - 1; i++) {
							data[0] += preData[i] + " ";
						}
						data[1] = preData[lastIndex];
						data[2] = null;
					} else {
						data[1] = null;
						data[2] = null;
						for (int i = 0; i < lastIndex; i++) {
							data[0] += preData[i] + " ";
						}
					}
					for (int i = 0; i < data.length; i++) {
						if (data[i] != null) {
							data[i] = data[i].trim();
						}
					}

					if (data[0].endsWith(",")) {
						data[0] = data[0].substring(0, data[0].length() - 1);
					}

					if (data[1] != null) {
						try {

							final String OMIMID = "OMIM:" + data[1];

							final String surfaceForm = data[0].trim().replaceAll("\\}|\\{|\\?", "");

							final Concept concept = new Concept(OMIMID, EOrigin.OMIM, EEntityType.DISEASE);

							final String normalizedSurfaceForm = Tokenizer.getTokenizedForm(surfaceForm);

							final String noVovalsNormalizedSurfaceForm = StringUtils.removeVowels(normalizedSurfaceForm);

							final DictionaryEntry entry = new DictionaryEntry(surfaceForm, normalizedSurfaceForm,
									noVovalsNormalizedSurfaceForm, true);

							dictionary.putIfAbsent(entry, new HashSet<>());
							dictionary.get(entry).add(concept);

							surfaceFormDict.putIfAbsent(normalizedSurfaceForm, new HashSet<>());
							surfaceFormDict.get(normalizedSurfaceForm).add(concept);
							final String sortedNormalizedSurfaceForm = StringUtils.sortTokens(normalizedSurfaceForm);

							sortedSurfaceFormDict.putIfAbsent(sortedNormalizedSurfaceForm, new HashSet<>());
							sortedSurfaceFormDict.get(sortedNormalizedSurfaceForm).add(concept);

							noVovalsSurfaceFormDict.putIfAbsent(noVovalsNormalizedSurfaceForm, new HashSet<>());
							noVovalsSurfaceFormDict.get(noVovalsNormalizedSurfaceForm).add(concept);

							tokensIndDict.addAll(Arrays.asList(normalizedSurfaceForm.split(" ")));

						} catch (final Exception e) {
							System.err.println(data[0]);
							System.err.println(data[1]);
							System.err.println("Could not read line: " + line);
						}
					}
				}
				br.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (Entry<DictionaryEntry, Set<Concept>> sd : getDictionary().entrySet()) {

			for (Concept concept : sd.getValue()) {
				conceptBasedDictionary.putIfAbsent(concept, new HashSet<>());
				conceptBasedDictionary.get(concept).add(sd.getKey().normalizedSurfaceForm);
			}
		}

	}

	@Override
	public boolean sortedNormalizedSurfaceFormMatchesConcept(String sortedSurfaceForm, Concept conceptID) {

		if (containsSurfaceForm(sortedSurfaceForm))
			return sortedSurfaceFormDict.get(sortedSurfaceForm).contains(conceptID);

		return false;
	}

	@Override
	public boolean containsSortedSurfaceForm(String sortedSurfaceForm) {
		return sortedSurfaceFormDict.keySet().contains(sortedSurfaceForm);
	}

	@Override
	public Set<Concept> getConceptsForSortedNormalizedSurfaceForm(String sortedSurfaceForm) {

		if (containsNoVowelsSurfaceForm(sortedSurfaceForm))
			return sortedSurfaceFormDict.get(sortedSurfaceForm);

		return new HashSet<>();
	}

	@Override
	public boolean noVowelsNormalizedSurfaceFormMatchesConcept(String noVovalsNormalizedSurfaceForm,
			Concept conceptID) {

		if (containsSurfaceForm(noVovalsNormalizedSurfaceForm))
			return noVovalsSurfaceFormDict.get(noVovalsNormalizedSurfaceForm).contains(conceptID);

		return false;
	}

	@Override
	public boolean containsNoVowelsSurfaceForm(String noVovalsNormalizedSurfaceForm) {
		return noVovalsSurfaceFormDict.keySet().contains(noVovalsNormalizedSurfaceForm);
	}

	@Override
	public Set<Concept> getConceptsForNoVowelsNormalizedSurfaceForm(String noVovalsNormalizedSurfaceForm) {

		if (containsNoVowelsSurfaceForm(noVovalsNormalizedSurfaceForm))
			return noVovalsSurfaceFormDict.get(noVovalsNormalizedSurfaceForm);

		return new HashSet<>();
	}

	@Override
	public boolean containsSurfaceForm(String normalizedSurfaceForm) {
		return surfaceFormDict.keySet().contains(normalizedSurfaceForm);
	}

	@Override
	public Set<Concept> getConceptsForNormalizedSurfaceForm(String normalizedSurfaceForm) {

		if (containsSurfaceForm(normalizedSurfaceForm))
			return surfaceFormDict.get(normalizedSurfaceForm);

		return new HashSet<>();
	}

	@Override
	public boolean normalizedSurfaceFormMatchesConcept(String normalizedSurfaceForm, Concept conceptID) {

		if (containsSurfaceForm(normalizedSurfaceForm))
			return surfaceFormDict.get(normalizedSurfaceForm).contains(conceptID);

		return false;
	}

	@Override
	public boolean containsToken(String token) {
		return tokensIndDict.contains(token);
	}

	@Override
	public Set<String> getMentionsForConcept(Concept concept) {
		return conceptBasedDictionary.getOrDefault(concept, new HashSet<>());
	}

	@Override
	public Set<String> getAllMentions() {
		return surfaceFormDict.keySet();
	}

	@Override
	public Map<Concept, Set<String>> getConceptBasedDictionary() {
		return conceptBasedDictionary;
	}
}

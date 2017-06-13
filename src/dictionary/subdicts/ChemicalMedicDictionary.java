package dictionary.subdicts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class ChemicalMedicDictionary implements ISubDictionary {

	public static void main(String[] args) {

		ChemicalMedicDictionary dict = new ChemicalMedicDictionary(4);

		dict.build();

		// dict.dictionary.entrySet().forEach(System.out::println);
	}

	final private static int MEDIC_INDEX_CHEMICAL_NAME = 0;
	final private static int MEDIC_INDEX_CHEMICAL_MAIN_ID = 1;
	final private static int MEDIC_INDEX_CHEMICAL_SYNONYMS = 7;

	private int priority = 5;

	public ChemicalMedicDictionary(int priority) {
		this.priority = priority;
	}

	final private Map<String, Set<Concept>> sortedSurfaceFormDict = new HashMap<>();

	final Map<DictionaryEntry, Set<Concept>> dictionary = new HashMap<>();
	final Map<String, Set<Concept>> surfaceFormDict = new HashMap<>();
	final Map<String, Set<Concept>> noVovalsSurfaceFormDict = new HashMap<>();
	final Set<String> tokensIndDict = new HashSet<>();
	final private Map<Concept, Set<String>> conceptBasedDictionary = new HashMap<>();

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
		final String MEDIC_INPUT_FILE_NAME = DatasetConfig.getChemicalMedicFilepath();
		try {
			BufferedReader br;
			br = new BufferedReader(new FileReader(new File(MEDIC_INPUT_FILE_NAME)));

			String line = "";

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty() || line.startsWith("#"))
					continue;

				String[] data = line.split("\t");

				if (data.length < 8)
					continue;

				String surfaceForm = data[MEDIC_INDEX_CHEMICAL_NAME];
				final String conceptID = data[MEDIC_INDEX_CHEMICAL_MAIN_ID].replaceAll("\"", "").trim();

				/*
				 * Skip OMIM Diseases...
				 */
				if (conceptID.startsWith("OMIM"))
					continue;

				if (surfaceForm.matches("\\d"))
					continue;

				if (surfaceForm.length() <= 1)
					continue;

				final Set<String> alternateIDs = new HashSet<>();

				final Concept concept = new Concept(conceptID, EOrigin.MEDIC, alternateIDs, EEntityType.CHEMICAL);

				final String normalizedSurfaceForm = Tokenizer.getTokenizedForm(surfaceForm);
				final String noVovalsNormalizedSurfaceForm = StringUtils.removeVowels(normalizedSurfaceForm);

				final DictionaryEntry entry = new DictionaryEntry(surfaceForm, normalizedSurfaceForm,
						noVovalsNormalizedSurfaceForm, true);

				addEntry(concept, normalizedSurfaceForm, noVovalsNormalizedSurfaceForm, entry);

				for (String synonym : data[MEDIC_INDEX_CHEMICAL_SYNONYMS].split("\\|")) {

					if (synonym.trim().matches("\\d"))
						continue;

					if (synonym.trim().length() <= 1)
						continue;

					final String normalizedSynonym = Tokenizer.getTokenizedForm(synonym);

					final String noVovalsNormalizedSynonym = StringUtils.removeVowels(normalizedSynonym);

					final DictionaryEntry synonymEntry = new DictionaryEntry(synonym, normalizedSynonym,
							noVovalsNormalizedSynonym, false);

					addEntry(concept, normalizedSynonym, noVovalsNormalizedSynonym, synonymEntry);

				}

			}
			br.close();
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

	private void addEntry(final Concept concept, final String normalizedSynonym,
			final String noVovalsNormalizedSurfaceForm, final DictionaryEntry synonymEntry) {

		dictionary.putIfAbsent(synonymEntry, new HashSet<>());
		dictionary.get(synonymEntry).add(concept);
		surfaceFormDict.putIfAbsent(normalizedSynonym, new HashSet<>());
		surfaceFormDict.get(normalizedSynonym).add(concept);
		noVovalsSurfaceFormDict.putIfAbsent(noVovalsNormalizedSurfaceForm, new HashSet<>());
		noVovalsSurfaceFormDict.get(noVovalsNormalizedSurfaceForm).add(concept);
		tokensIndDict.addAll(Arrays.asList(normalizedSynonym.split(" ")));

		final String sortedNormalizedSurfaceForm = StringUtils.sortTokens(normalizedSynonym);

		sortedSurfaceFormDict.putIfAbsent(sortedNormalizedSurfaceForm, new HashSet<>());
		sortedSurfaceFormDict.get(sortedNormalizedSurfaceForm).add(concept);
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

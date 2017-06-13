package dictionary.subdicts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import corpus.DataReader;

import java.util.Set;

import dictionary.Concept;
import dictionary.DictionaryEntry;
import dictionary.EOrigin;
import main.setting.EDataset;
import tokenization.Tokenizer;
import util.StringUtils;
import variables.EEntityType;

public class DiseaseDatasetDictionary implements ISubDictionary {

	public static void main(String[] args) {
		DiseaseDatasetDictionary dict = new DiseaseDatasetDictionary(EDataset.TRAIN, 4);

		dict.build();

		dict.dictionary.entrySet().forEach(System.out::println);
	}

	private int priority = 4;

	final private Map<DictionaryEntry, Set<Concept>> dictionary = new HashMap<>();

	final private Map<String, Set<Concept>> surfaceFormDict = new HashMap<>();

	final private Map<String, Set<Concept>> sortedSurfaceFormDict = new HashMap<>();

	final Map<String, Set<Concept>> noVovalsSurfaceFormDict = new HashMap<>();

	final private Set<String> tokensIndDict = new HashSet<>();

	final private EDataset dataset;

	final private Map<Concept, Set<String>> conceptBasedDictionary = new HashMap<>();

	final EOrigin origin;

	public DiseaseDatasetDictionary(final EDataset dataset, final int priority) {
		this.dataset = dataset;
		this.priority = priority;
		switch (dataset) {
		case TEST:
			System.err.println(DiseaseDatasetDictionary.class.getSimpleName()
					+ ": USAGE OF TESTSET DETECTED WHILE GENERATING DICTIONARY!");
			origin = EOrigin.TEST;
			break;
		case TRAIN:
			origin = EOrigin.TRAIN;
			break;
		case DEVELOP:
			origin = EOrigin.DEV;
			break;
		default:
			origin = null;
			break;
		}

	}

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
			 * ConceptID, SurfaceForms
			 */
			for (Entry<String, Set<String>> datasetEntry : DataReader.getDiseaseSurfaceFormToIDMapping(dataset)
					.entrySet()) {

				String conceptID = datasetEntry.getKey();

				for (final String surfaceForm : datasetEntry.getValue()) {

					final Concept concept = new Concept(conceptID, origin, EEntityType.DISEASE);

					final String normalizedSurfaceForm = Tokenizer.getTokenizedForm(surfaceForm);
					final String noVovalsNormalizedSurfaceForm = StringUtils.removeVowels(normalizedSurfaceForm);

					final DictionaryEntry entry = new DictionaryEntry(surfaceForm, normalizedSurfaceForm,
							noVovalsNormalizedSurfaceForm, true);

					dictionary.putIfAbsent(entry, new HashSet<>());
					dictionary.get(entry).add(concept);

					tokensIndDict.addAll(Arrays.asList(normalizedSurfaceForm.split(" ")));

					surfaceFormDict.putIfAbsent(normalizedSurfaceForm, new HashSet<>());
					surfaceFormDict.get(normalizedSurfaceForm).add(concept);

					final String sortedNormalizedSurfaceForm = StringUtils.sortTokens(normalizedSurfaceForm);

					sortedSurfaceFormDict.putIfAbsent(sortedNormalizedSurfaceForm, new HashSet<>());
					sortedSurfaceFormDict.get(sortedNormalizedSurfaceForm).add(concept);

					noVovalsSurfaceFormDict.putIfAbsent(noVovalsNormalizedSurfaceForm, new HashSet<>());
					noVovalsSurfaceFormDict.get(noVovalsNormalizedSurfaceForm).add(concept);

				}
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

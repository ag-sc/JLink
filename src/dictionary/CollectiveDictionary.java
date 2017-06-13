package dictionary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dictionary.subdicts.ISubDictionary;
import main.JLink;
import main.Main;
import main.ParameterReader;
import main.param.Parameter;

public class CollectiveDictionary implements ICollectiveDictionary {

	public static void main(String[] args) throws FileNotFoundException {

		Parameter parameter;

		if (args == null || args.length == 0) {
			parameter = ParameterReader.defaultParameters();
		} else {
			parameter = ParameterReader.readParametersFromCommandLine(args);
		}
		new JLink(Main.buildSettings(parameter));

		CollectiveDictionary dict = CollectiveDictionaryFactory.getInstance();

		Set<Concept> c = new HashSet<>();
		for (String string : dict.getAllSurfaceForms()) {

			c.addAll(dict.getConceptsForNormalizedSurfaceForm(string));

		}
		// for (ISubDictionary iDict : dict.getDictionaries()) {
		// for (Entry<DictionaryEntry, Set<Concept>> d :
		// iDict.getDictionary().entrySet()) {
		//
		// if (d.getValue().size() > 1) {
		// System.out.println(d);
		// }
		// }
		//
		// }
		// writeDictionaryToFileSystem(dict);
	}

	private static void writeDictionaryToFileSystem(CollectiveDictionary dict) throws FileNotFoundException {
		PrintStream ps = new PrintStream(new File("gen/collectiveDiseaseDictionary"));

		Map<Concept, Set<String>> d = new HashMap<>();

		for (ISubDictionary subDict : dict.getDictionaries()) {

			for (Entry<DictionaryEntry, Set<Concept>> sd : subDict.getDictionary().entrySet()) {

				for (Concept concept : sd.getValue()) {
					d.putIfAbsent(concept, new HashSet<>());
					d.get(concept).add(sd.getKey().normalizedSurfaceForm);
				}
			}
		}

		d.entrySet().stream().map(c -> c.getKey().conceptID + "=" + c.getValue()).forEach(ps::println);

		ps.close();
	}

	protected CollectiveDictionary() {
	}

	private List<ISubDictionary> dictionaries = new ArrayList<>();

	@Override
	public boolean containsNoVowelsSurfaceForm(final String normalizedSurfaceForm) {
		for (ISubDictionary iSubDictionary : dictionaries) {
			if (iSubDictionary.containsNoVowelsSurfaceForm(normalizedSurfaceForm)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Concept> getConceptsForNoVowelsNormalizedSurfaceForm(final String normalizedSurfaceForm) {

		Set<Concept> concepts = new HashSet<>();

		for (ISubDictionary iSubDictionary : dictionaries) {
			concepts.addAll(iSubDictionary.getConceptsForNoVowelsNormalizedSurfaceForm(normalizedSurfaceForm));
		}
		return concepts;
	}

	@Override
	public boolean containsEntry(DictionaryEntry entry) {
		for (ISubDictionary iSubDictionary : dictionaries) {
			if (iSubDictionary.getDictionary().containsKey(entry))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsSurfaceForm(final String normalizedSurfaceForm) {
		for (ISubDictionary iSubDictionary : dictionaries) {
			if (iSubDictionary.containsSurfaceForm(normalizedSurfaceForm)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Concept> getConceptsForNormalizedSurfaceForm(final String normalizedSurfaceForm) {

		Set<Concept> concepts = new HashSet<>();

		for (ISubDictionary iSubDictionary : dictionaries) {
			concepts.addAll(iSubDictionary.getConceptsForNormalizedSurfaceForm(normalizedSurfaceForm));
		}
		return concepts;
	}

	protected void addDictionary(ISubDictionary subDict) {
		dictionaries.add(subDict);
	}

	@Override
	public void build() {

		for (ISubDictionary iSubDictionary : dictionaries) {
			iSubDictionary.build();
		}

		Collections.sort(dictionaries, new Comparator<ISubDictionary>() {

			@Override
			public int compare(ISubDictionary o1, ISubDictionary o2) {
				return -Integer.compare(o1.getPriority(), o1.getPriority());
			}
		});

	}

	@Override
	public boolean normalizedSurfaceFormMatchesConcept(String normalizedSurfaceForm, Concept concept) {
		for (ISubDictionary iSubDictionary : dictionaries) {
			if (iSubDictionary.normalizedSurfaceFormMatchesConcept(normalizedSurfaceForm, concept)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsToken(String token) {
		for (ISubDictionary iSubDictionary : dictionaries) {
			if (iSubDictionary.containsToken(token)) {
				return true;
			}
		}
		return false;
	}

	public List<ISubDictionary> getDictionaries() {
		return dictionaries;
	}

	public Set<String> getMentionsForConcept(final Concept concept) {

		Set<String> mentions = new HashSet<>();

		for (ISubDictionary iSubDictionary : dictionaries) {
			mentions.addAll(iSubDictionary.getMentionsForConcept(concept));
		}

		return mentions;
	}

	public Set<String> getAllSurfaceForms() {
		Set<String> mentions = new HashSet<>();

		for (ISubDictionary iSubDictionary : dictionaries) {
			mentions.addAll(iSubDictionary.getAllMentions());
		}

		return mentions;
	}

	@Override
	public boolean noVowelsNormalizedSurfaceFormMatchesConcept(String noVovalsNormalizedSurfaceForm, Concept concept) {
		for (ISubDictionary iSubDictionary : dictionaries) {
			if (iSubDictionary.noVowelsNormalizedSurfaceFormMatchesConcept(noVovalsNormalizedSurfaceForm, concept)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean sortedNormalizedSurfaceFormMatchesConcept(String sortedSurfaceForm, Concept conceptID) {

		for (ISubDictionary iSubDictionary : dictionaries) {
			if (iSubDictionary.sortedNormalizedSurfaceFormMatchesConcept(sortedSurfaceForm, conceptID)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsSortedSurfaceForm(String sortedSurfaceForm) {
		for (ISubDictionary iSubDictionary : dictionaries) {
			if (iSubDictionary.containsSortedSurfaceForm(sortedSurfaceForm)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Concept> getConceptsForSortedNormalizedSurfaceForm(String sortedSurfaceForm) {

		Set<Concept> concepts = new HashSet<>();

		for (ISubDictionary iSubDictionary : dictionaries) {
			concepts.addAll(iSubDictionary.getConceptsForSortedNormalizedSurfaceForm(sortedSurfaceForm));
		}
		return concepts;
	}

}

package dictionary.subdicts;

import java.util.Map;
import java.util.Set;

import dictionary.Concept;
import dictionary.DictionaryEntry;
import dictionary.ICollectiveDictionary;

public interface ISubDictionary extends ICollectiveDictionary {

	public int getPriority();

	public void setPriority(final int priority);

	public Map<DictionaryEntry, Set<Concept>> getDictionary();

	public Set<String> getMentionsForConcept(Concept concept);

	public Set<String> getAllMentions();

	public Map<Concept, Set<String>> getConceptBasedDictionary();

}

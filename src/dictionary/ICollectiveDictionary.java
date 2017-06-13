package dictionary;

import java.util.Set;

public interface ICollectiveDictionary {

	public boolean containsEntry(DictionaryEntry entry);

	public void build();

	public boolean containsSurfaceForm(final String normalizedSurfaceForm);

	public Set<Concept> getConceptsForNormalizedSurfaceForm(final String normalizedSurfaceForm);

	public boolean normalizedSurfaceFormMatchesConcept(String normalizedSurfaceForm, Concept conceptID);

	public boolean containsToken(String token);

	public boolean noVowelsNormalizedSurfaceFormMatchesConcept(String noVowelsNormalizedSurfaceForm, Concept conceptID);

	public boolean containsNoVowelsSurfaceForm(String noVowelsNormalizedSurfaceForm);

	public Set<Concept> getConceptsForNoVowelsNormalizedSurfaceForm(String noVowelsNormalizedSurfaceForm);

	public boolean sortedNormalizedSurfaceFormMatchesConcept(String sortedSurfaceForm, Concept conceptID);

	public boolean containsSortedSurfaceForm(String sortedSurfaceForm);

	public Set<Concept> getConceptsForSortedNormalizedSurfaceForm(String sortedSurfaceForm);

}

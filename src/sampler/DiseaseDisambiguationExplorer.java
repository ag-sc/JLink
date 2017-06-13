package sampler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import candidateretrieval.ICandidateRetrieval;
import candidateretrieval.jaccard.JaccardCandidate;
import candidateretrieval.jaccard.JaccardRetrieval;
import candidateretrieval.levenshtein.LevenshteinCandidate;
import candidateretrieval.levenshtein.LevenshteinRetrieval;
import candidateretrieval.lucene.LuceneCandidate;
import candidateretrieval.lucene.LuceneRetrieval;
import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import sampling.Explorer;
import tokenization.Tokenizer;
import variables.JLinkState;
import variables.EntityAnnotation;

public class DiseaseDisambiguationExplorer implements Explorer<JLinkState> {

	private static Logger log = LogManager.getFormatterLogger(DiseaseDisambiguationExplorer.class.getName());

	private JaccardRetrieval NgramRetrieval;
	private LevenshteinRetrieval levenshteinRetrieval;
	private LuceneRetrieval luceneRetrieval;

	private final CollectiveDictionary dict;

	public static void main(String[] args) {
		DiseaseDisambiguationExplorer explorer = new DiseaseDisambiguationExplorer(true, true, true);

		explorer.getRelevantIDs("leukaemia").forEach(System.out::println);
	}

	private static boolean includeLuceneCandidateRetrieval;
	private static boolean includeJaccardCandidateRetrieval;
	private static boolean includeLevenshteinCandidateRetrieval;

	private DiseaseDisambiguationExplorer(final boolean includeLuceneCandidateRetrieval,
			final boolean includeJaccardCandidateRetrieval, final boolean includeLevenshteinCandidateRetrieval) {

		dict = CollectiveDictionaryFactory.getInstance();

		DiseaseDisambiguationExplorer.includeLuceneCandidateRetrieval = includeLuceneCandidateRetrieval;
		DiseaseDisambiguationExplorer.includeJaccardCandidateRetrieval = includeJaccardCandidateRetrieval;
		DiseaseDisambiguationExplorer.includeLevenshteinCandidateRetrieval = includeLevenshteinCandidateRetrieval;

		if (includeLuceneCandidateRetrieval)
			luceneRetrieval = LuceneRetrieval.getInstance();
		if (includeJaccardCandidateRetrieval)
			NgramRetrieval = JaccardRetrieval.getInstance();
		if (includeLevenshteinCandidateRetrieval)
			levenshteinRetrieval = LevenshteinRetrieval.getInstance();

	}

	public DiseaseDisambiguationExplorer(Map<Class<? extends ICandidateRetrieval>, Boolean> candidateRetrievalSetting) {

		this(candidateRetrievalSetting.get(LuceneRetrieval.class),
				candidateRetrievalSetting.get(JaccardRetrieval.class),
				candidateRetrievalSetting.get(LevenshteinRetrieval.class));
	}

	@Override
	public List<JLinkState> getNextStates(JLinkState previousState) {

		List<JLinkState> generatedStates = new ArrayList<>();

		/*
		 * Modify existing entities
		 */
		Collection<EntityAnnotation> previousStatesEntities = previousState.getEntities();

		Set<EntityAnnotation> alreadyAssignedTypes = new HashSet<>();

		for (EntityAnnotation previousStatesEntity : previousStatesEntities) {
			alreadyAssignedTypes.add(previousStatesEntity);
		}

		/**
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * TEST THIS
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		// for (EntityAnnotation forSortFormspreviousStatesEntity :
		// alreadyAssignedTypes) {
		// /*
		// * TODO: sample on abbreviations over all assigned concepts.
		// */
		// State generatedState = new State(previousState);
		// EntityAnnotation entity =
		// generatedState.getEntity(forSortFormspreviousStatesEntity.getEntityID());
		// entity.setType(forSortFormspreviousStatesEntity.getType());
		// generatedStates.add(generatedState);
		// }

		/**
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		// boolean includeMorphologicalTransformations = true;

		for (EntityAnnotation previousStatesEntity : previousStatesEntities) {

			Collection<Concept> diseaseIDs = getRelevantIDs(previousStatesEntity.getText());
			/*
			 * remove the type that this entity already has assigned
			 */
			diseaseIDs.remove(previousStatesEntity.getType());
			/*
			 * change Type of every entity to every possible type
			 */

			for (Concept diseaseID : diseaseIDs) {

				JLinkState generatedState = new JLinkState(previousState);
				EntityAnnotation entity = generatedState.getEntity(previousStatesEntity.getEntityID());
				entity.setType(diseaseID);
				generatedStates.add(generatedState);

				// if (includeMorphologicalTransformations) {
				// dict.getMentionsForConcept(diseaseID.getConcept()).stream().forEach(dictionaryEntry
				// -> {
				//
				// CharacterRule rule = MorphologicalTransformationTemplate
				// .extractMorphologicalRule(previousStatesEntity.getText(),
				// dictionaryEntry);
				//
				// if (rule != null && knownRules.contains(rule.forwardRule)) {
				//
				// DregonState gs = new DregonState(previousState);
				//
				// EntityAnnotation e = new EntityAnnotation(gs,
				// gs.getEntity(previousStatesEntity.getEntityID()),
				// dictionaryEntry);
				//
				// gs.addEntity(entity);
				// System.out.println("Generate new state with: " +
				// previousStatesEntity.getText()
				// + " replaced by " + dictionaryEntry);
				// generatedStates.add(gs);
				// }
				//
				// });
				// }

			}
			/*
			 * Create on state with that particular entity removed
			 */
			// DregonState generatedState = new DregonState(previousState);
			// generatedState.removeEntity(previousStatesEntity.getEntityID());
			// generatedStates.add(generatedState);
		}

		return generatedStates;
	}

	public Collection<Concept> getRelevantIDs(final String possibleDiseaseMention) {
		final String tokenizedMapping = Tokenizer.getTokenizedForm(possibleDiseaseMention);
		// System.out.println("tokenizedMapping = " + tokenizedMapping);
		Collection<Concept> possibleDiseaseIDs = getRelevantIDsByStringMatch(tokenizedMapping);
		// System.out.println("possibleDiseaseIDs = " + possibleDiseaseIDs);
		// System.out.println("possibleDiseaseMention = " +
		// possibleDiseaseMention);
		if (includeLuceneCandidateRetrieval)
			possibleDiseaseIDs.addAll(getRelevantIDsByLucene(tokenizedMapping));
		if (includeJaccardCandidateRetrieval)
			possibleDiseaseIDs.addAll(getRelevantIDsByNgram(tokenizedMapping));
		if (includeLevenshteinCandidateRetrieval)
			possibleDiseaseIDs.addAll(getRelevantIDsLevenShtein(tokenizedMapping));
		// possibleDiseaseIDs.forEach(System.out::println);
		// System.out.println("possibleDiseaseIDs = " + possibleDiseaseIDs);
		return new HashSet<>(possibleDiseaseIDs);

	}

	private Collection<Concept> getRelevantIDsByStringMatch(final String possibleDiseaseMention) {
		Collection<Concept> possibleDiseaseIDs = new HashSet<Concept>();
		Set<Concept> concepts = dict.getConceptsForNormalizedSurfaceForm(possibleDiseaseMention);

		for (Concept concept : concepts) {
			possibleDiseaseIDs.add(new Concept(concept));
		}

		return possibleDiseaseIDs;
	}

	private Collection<Concept> getRelevantIDsByNgram(String ann) {

		Collection<Concept> possibleDiseaseIDs = new HashSet<Concept>();
		for (JaccardCandidate s : NgramRetrieval.getCandidates(ann)) {
			possibleDiseaseIDs.add(new Concept(s.concept));
		}

		return possibleDiseaseIDs;
	}

	private Collection<Concept> getRelevantIDsLevenShtein(String annotation) {
		Collection<Concept> possibleDiseaseIDs = new HashSet<Concept>();
		for (LevenshteinCandidate s : levenshteinRetrieval.getCandidates(annotation)) {
			possibleDiseaseIDs.add(new Concept(s.concept));
		}

		return possibleDiseaseIDs;
	}

	private Collection<Concept> getRelevantIDsByLucene(final String possibleDiseaseMention) {

		Collection<Concept> possibleDiseaseIDs = new HashSet<Concept>();
		for (LuceneCandidate luceneCandidate : luceneRetrieval.getFuzzyCandidates(possibleDiseaseMention)) {
			possibleDiseaseIDs.add(new Concept(luceneCandidate.concept));
		}
		// possibleDiseaseIDs.add(new Concept("recognitionID"));

		return possibleDiseaseIDs;
	}
}

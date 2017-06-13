package templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import candidateretrieval.jaccard.JaccardRetrieval;
import candidateretrieval.levenshtein.LevenshteinRetrieval;
import candidateretrieval.lucene.LuceneCandidate;
import candidateretrieval.lucene.LuceneRetrieval;
import dictionary.Concept;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import templates.CadidateRetrievalTemplate.Scope;
import util.StringUtils;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class CadidateRetrievalTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(CadidateRetrievalTemplate.class.getName());

	class Scope extends FactorScope {

		private Concept Concept;
		private String text;

		public Scope(AbstractTemplate<?, ?, Scope> template, Concept Concept, String text) {
			super(template, Concept, text);
			this.Concept = Concept;
			this.text = text;
		}

		public Concept getConcept() {
			return Concept;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "Scope [Concept=" + Concept + ", text=" + text + "]";
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		for (EntityAnnotation entity : state.getEntities()) {
			factors.add(new Scope(this, entity.getType(), entity.getText()));
		}
		return factors;
	}

	transient private static JaccardRetrieval NgramRetrieval;
	transient private static LevenshteinRetrieval levenshteinRetrieval;
	transient private static LuceneRetrieval luceneRetrieval;

	public CadidateRetrievalTemplate() {
		// NgramRetrieval = JaccardRetrieval.getInstance();
		// levenshteinRetrieval = LevenShteinCandidateRetrieval.getInstance();
		luceneRetrieval = LuceneRetrieval.getInstance();
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Vector featureVector = factor.getFeatureVector();

		final Concept concept = factor.getFactorScope().getConcept();

		final String cleanedAnnotation = factor.getFactorScope().getText();

		final boolean isAbbreviation = StringUtils.isAbbreviation(cleanedAnnotation);

		/*
		 * Do not use this feature for abbreviations.
		 */

		lucene(featureVector, concept, cleanedAnnotation, isAbbreviation);

		// final Set<Concept> jaccardResult =
		// NgramRetrieval.getCandidates(cleanedAnnotation).stream().map(j
		// -> j.concept)
		// .collect(Collectors.toSet());

		// final Set<Concept> levenshteinResult =
		// levenshteinRetrieval.getCandidates(cleanedAnnotation).stream()
		// .map(j -> j.concept).collect(Collectors.toSet());

		// if (concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
		// return;
		// featureVector.set("JACCARD_RETRIEVAL_MATCHES_TEXT",
		// !jaccardResult.isEmpty());

		// featureVector.set("LEVENSHTEIN_RETRIEVAL_MATCHES_TEXT",
		// !levenshteinResult.isEmpty());

		// final boolean inJaccardRetrieval = jaccardResult.contains(concept);

		// final boolean inLevenshteinRetrieval =
		// levenshteinResult.contains(concept);

		// featureVector.set("JACCARD_RETRIEVAL_MATCHES_ID",
		// inJaccardRetrieval);
		// featureVector.set("LEVENSHTEIN_RETRIEVAL_MATCHES_ID",
		// inLevenshteinRetrieval);

	}

	private void lucene(Vector featureVector, final Concept concept, final String cleanedAnnotation,
			final boolean isAbbreviation) {

		List<LuceneCandidate> candidates = luceneRetrieval.getFuzzyCandidates(cleanedAnnotation);

		if (isAbbreviation)
			featureVector.set("ABBREVIATION_LUCENE_RETRIEVAL_MATCHES_TEXT", !candidates.isEmpty());
		else
			featureVector.set("LUCENE_RETRIEVAL_MATCHES_TEXT", !candidates.isEmpty());

		// if (concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
		// return;
		//
		// for (int rank = 0; rank < candidates.size(); rank++) {
		// if (candidates.get(rank).concept.equals(concept)) {
		// featureVector.set("LUCENE_RETRIEVAL_MATCHES_ID", true);
		// break;
		// }
		// }
	}

}

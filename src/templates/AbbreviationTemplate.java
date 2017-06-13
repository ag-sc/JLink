package templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.Concept;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import sampler.MultipleTokenBoundaryExplorer;
import templates.AbbreviationTemplate.Scope;
import util.StringUtils;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class AbbreviationTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope> {

	private static Logger log = LogManager.getFormatterLogger(AbbreviationTemplate.class.getName());

	static class Scope extends FactorScope {

		private final Set<String> matchingLongforms;
		private final Concept Concept;
		private final String text;

		public Scope(AbstractTemplate<?, ?, Scope> template, Set<String> matchingLongforms, Concept Concept,
				String text) {
			super(template, matchingLongforms, Concept, text);
			this.matchingLongforms = matchingLongforms;
			this.Concept = Concept;
			this.text = text;
		}

		public Set<String> getMatchingLongforms() {
			return matchingLongforms;
		}

		public Concept getConcept() {
			return Concept;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "Scope [matchingLongforms=" + matchingLongforms + ", Concept=" + Concept + ", text=" + text + "]";
		}
	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		for (EntityAnnotation entity : state.getEntities()) {

			if (!StringUtils.isAbbreviation(entity.getText()))
				continue;

			if (entity.getType().equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
				continue;

			factors.add(new Scope(this,
					entity.getState().getEntities().stream()
							/*
							 * Include all mentions that share the same concept
							 * but not the same surface form.
							 */
							.filter(e -> e != entity && !e.getText().equals(entity.getText())
									&& e.getType().equals(entity.getType()))
							.map(e -> e.getText()).collect(Collectors.toSet()),
					entity.getType(), entity.getText()));
		}
		return factors;
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		final Vector featureVector = factor.getFeatureVector();

		/*
		 * TEST ON: CDR_developset_corpus_16584858 TODO: get left next token
		 */

		featureVector.set(factor.getFactorScope().getText() + "_IS_ABBREVIATION", true);

		featureVector.set(factor.getFactorScope().getText() + "_IS_ABBREVIATION_FOR_"
				+ factor.getFactorScope().getConcept().getConceptID(), true);

		final boolean longFormFound = !factor.getFactorScope().getMatchingLongforms().isEmpty();

		for (String longForm : factor.getFactorScope().getMatchingLongforms()) {
			featureVector.set(factor.getFactorScope().getText() + "==" + longForm, true);
		}

		featureVector.set("ABBREVIATION_AND_LONGFORM", longFormFound);
		// featureVector.set("ABBREVIATION_AND_NOT_LONGFORM", !longFormFound);

	}

}

package templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.Concept;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import sampler.MultipleTokenBoundaryExplorer;
import templates.TrainingPriorTemplate.Scope;
import tokenization.Tokenizer;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class TrainingPriorTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(TrainingPriorTemplate.class.getName());

	static class Scope extends FactorScope {

		private boolean isSyn;
		private Concept Concept;
		private String text;

		public Scope(AbstractTemplate<?, ?, Scope> template, boolean isSyn, Concept Concept, String text) {
			super(template, isSyn, Concept, text);
			this.isSyn = isSyn;
			this.Concept = Concept;
			this.text = text;
		}

		public boolean isSyntacticTransformation() {
			return isSyn;
		}

		public Concept getConcept() {
			return Concept;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "Scope [isSyn=" + isSyn + ", Concept=" + Concept + ", text=" + text + "]";
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		for (EntityAnnotation entity : state.getEntities()) {
			factors.add(new Scope(this, entity.isSyntacticTransformation(), entity.getType(), entity.getText()));
		}
		return factors;
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Vector featureVector = factor.getFeatureVector();

		if (factor.getFactorScope().isSyntacticTransformation())
			return;

		final String text = Tokenizer.getTokenizedForm(factor.getFactorScope().getText());
		// final String textNoVowels = StringUtil.removeVowels(text);
		featureVector.set("TRAIN_PRIOR_" + text, true);
		// featureVector.set("TRAIN_PRIOR_NO_VOWELS_" + textNoVowels, true);

		final Concept concept = factor.getFactorScope().getConcept();

		if (concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
			return;

		featureVector.set("TRAIN_PRIOR_" + concept.conceptID, true);
		featureVector.set("TRAIN_PRIOR_" + text + "=" + concept.conceptID, true);
		// featureVector.set("TRAIN_PRIOR_NO_VOWELS_" + textNoVowels + "=" +
		// concept.conceptID, true);
	}

}

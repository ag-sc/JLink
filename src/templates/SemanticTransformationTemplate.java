package templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import templates.SemanticTransformationTemplate.Scope;
import tokenization.Tokenizer;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class SemanticTransformationTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(SemanticTransformationTemplate.class.getName());

	static class Scope extends FactorScope {

		final boolean isSyntacticTransformation;
		final String realToken;
		final String synonymToken;

		public Scope(AbstractTemplate<?, ?, Scope> template, String realToken, String synonymToken,
				boolean isSyntacticTransformation) {
			super(template, realToken, synonymToken, isSyntacticTransformation);
			this.realToken = realToken;
			this.synonymToken = synonymToken;
			this.isSyntacticTransformation = isSyntacticTransformation;
		}

		public boolean isSyntacticTransformation() {
			return isSyntacticTransformation;
		}

		@Override
		public String toString() {
			return "Scope [isSyntacticTransformation=" + isSyntacticTransformation + ", realToken=" + realToken
					+ ", synonymToken=" + synonymToken + "]";
		}

		public String getRealToken() {
			return realToken;
		}

		public String getSynonymToken() {
			return synonymToken;
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		for (EntityAnnotation entity : state.getEntities()) {
			factors.add(new Scope(this, entity.getRealToken(), entity.getSynonymReplacementToken(),
					entity.isSyntacticTransformation()));
		}
		return factors;
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Vector featureVector = factor.getFeatureVector();

		final boolean isSyntacticTransformation = factor.getFactorScope().isSyntacticTransformation();

		if (isSyntacticTransformation) {
			final String realToken = Tokenizer.getTokenizedForm(factor.getFactorScope().getRealToken());
			final String synonymToken = Tokenizer.getTokenizedForm(factor.getFactorScope().getSynonymToken());

			featureVector.set(realToken + "_SYNTACTIC_TRANSFORMATION_" + synonymToken, true);
			// featureVector.set(realToken +
			// "_SYNTACTIC_TRANSFORMATION_GENERAL_CLASS", true);
			// } else {
			// featureVector.set("NO_SYNTACTIC_TRANSFORMATION", true);
		}

	}

}

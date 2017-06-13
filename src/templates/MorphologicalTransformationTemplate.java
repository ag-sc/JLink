package templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import templates.MorphologicalTransformationTemplate.Scope;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class MorphologicalTransformationTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(MorphologicalTransformationTemplate.class.getName());

	public MorphologicalTransformationTemplate() {
		System.out.print("Prepare Morphological Transformation Template...");
		System.out.println(" done!");
	}

	static class Scope extends FactorScope {

		final boolean isMorphologicalTransformation;
		final String forwardRule;

		public Scope(AbstractTemplate<?, ?, Scope> template, String forwardRule,
				boolean isMorphologicalTransformation) {
			super(template, forwardRule, isMorphologicalTransformation);
			this.forwardRule = forwardRule;
			this.isMorphologicalTransformation = isMorphologicalTransformation;
		}

		public boolean isMorphologicalTransformation() {
			return isMorphologicalTransformation;
		}

		public String getForwardRule() {
			return forwardRule;
		}

		@Override
		public String toString() {
			return "Scope [isMorphologicalTransformation=" + isMorphologicalTransformation + ", forwardRule="
					+ forwardRule + "]";
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		for (EntityAnnotation entity : state.getEntities()) {
			factors.add(new Scope(this, entity.getForwardRule(), entity.isMorphologicalTransformation()));
		}
		return factors;
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Vector featureVector = factor.getFeatureVector();

		final boolean isMorphologicalTransformation = factor.getFactorScope().isMorphologicalTransformation();
		if (isMorphologicalTransformation) {

			final String forwardRule = factor.getFactorScope().getForwardRule();

			featureVector.set("MORPH_TRANSFORMATION_" + forwardRule, true);
		}
		// featureVector.set("MORPHOLOGIC_TRANSFORMATION",
		// isMorphologicalTransformation);
	}

}

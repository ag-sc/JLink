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
import templates.AnnotationTextTemplate.Scope;
import tokenization.Tokenizer;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class AnnotationTextTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(AnnotationTextTemplate.class.getName());

	static class Scope extends FactorScope {

		private Concept Concept1;
		private Concept Concept2;

		public Scope(AbstractTemplate<?, ?, Scope> template, Concept Concept1, Concept Concept2) {
			super(template, Concept1, Concept2);
			this.Concept1 = Concept1;
			this.Concept2 = Concept2;
		}

		public Concept getConcept1() {
			return Concept1;
		}

		public void setConcept1(Concept Concept1) {
			this.Concept1 = Concept1;
		}

		public Concept getConcept2() {
			return Concept2;
		}

		public void setConcept2(Concept Concept2) {
			this.Concept2 = Concept2;
		}

		@Override
		public String toString() {
			return "Scope [Concept1=" + Concept1 + ", Concept2=" + Concept2 + "]";
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();

		if (state.getEntities().size() > 1) {
			List<EntityAnnotation> es = new ArrayList<>(state.getEntities());
			for (int i = 0; i < es.size(); i++) {
				EntityAnnotation firstAnnotation = es.get(i);
				for (int j = i + 1; j < es.size(); j++) {
					EntityAnnotation secondAnnotation = es.get(j);

					/*
					 * One of them must be non UNK
					 */
					if (!firstAnnotation.getType().equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT)
							|| !secondAnnotation.getType().equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT)) {
						if (Tokenizer.getTokenizedForm(firstAnnotation.getText()).equals(secondAnnotation.getText())) {
							factors.add(new Scope(this, firstAnnotation.getType(), secondAnnotation.getType()));
						}
					}
				}
			}
		}
		return factors;

	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Concept firstEntity = factor.getFactorScope().getConcept1();
		Concept secondEntity = factor.getFactorScope().getConcept2();

		Vector featureVector = factor.getFeatureVector();

		boolean sameConceptAnnotated = firstEntity.equals(secondEntity);

		if (!sameConceptAnnotated) {
			/*
			 * If one of them is UNK then its good to annotate the other token.
			 */
			if (firstEntity.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT)
					|| secondEntity.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT)) {
				sameConceptAnnotated = true;
			}
		}

		featureVector.set("SAME_TEXTS_SAME_CONCEPTS", sameConceptAnnotated);

	}

}

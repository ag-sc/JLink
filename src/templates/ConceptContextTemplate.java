package templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.Concept;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import main.Main;
import sampler.MultipleTokenBoundaryExplorer;
import templates.ConceptContextTemplate.Scope;
import variables.EntityAnnotation;
import variables.JLinkState;
import variables.LabeledJlinkDocument;

public class ConceptContextTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(ConceptContextTemplate.class.getName());

	static class Scope extends FactorScope {

		private Concept Concept1;
		private Concept Concept2;

		public Scope(AbstractTemplate<?, ?, Scope> template, EntityAnnotation variable1, Concept Concept1, String text1,
				EntityAnnotation variable2, Concept Concept2, String text2) {
			super(template, Concept1, Concept2);
			this.Concept1 = Concept1;
			this.Concept2 = Concept2;
		}

		public Concept getConcept1() {
			return Concept1;
		}

		public Concept getConcept2() {
			return Concept2;
		}

		@Override
		public String toString() {
			return "Scope [Concept1=" + Concept1 + ", Concept2=" + Concept2 + "]";
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {

		List<Scope> factors = new ArrayList<>();
		Set<EntityAnnotation> typeSet = new HashSet<>(state.getEntities());

		for (EntityAnnotation firstAnnotation : typeSet) {

			if (firstAnnotation.getType().equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
				continue;

			for (EntityAnnotation secondAnnotation : typeSet) {

				if (secondAnnotation.getType().equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
					continue;

				factors.add(new Scope(this, firstAnnotation, firstAnnotation.getType(), firstAnnotation.getText(),
						secondAnnotation, secondAnnotation.getType(), secondAnnotation.getText()));
			}
		}

		return factors;

	}

	@Override
	public void computeFactor(Factor<Scope> factor) {
		Vector featureVector = factor.getFeatureVector();

		final Concept diseaseID1 = factor.getFactorScope().getConcept1();
		final Concept diseaseID2 = factor.getFactorScope().getConcept2();

		if (Main.DEBUG) {
			System.out.println("diseaseID1 = " + diseaseID1);
			System.out.println("diseaseID2 = " + diseaseID2);
			System.out.println("");
		}

		featureVector.set(diseaseID1.conceptID + "_APPEARS_WITH_" + diseaseID2.conceptID, true);
	}

}

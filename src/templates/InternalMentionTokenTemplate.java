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
import sampler.MultipleTokenBoundaryExplorer;
import templates.InternalMentionTokenTemplate.Scope;
import variables.JLinkState;
import variables.EEntityType;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

/**
 * This template generates features that captures the internal structure of
 * surface form to a specific disease.
 * 
 * Given a disease = "breast and ovarian cancer" and a assigned concept
 * "MESH:D061325" this template would generate features:
 * 
 * [breast and ovarian cancer $, MESH:D061325_breast and ovarian cancer, and
 * ovarian cancer $, ovarian, breast and ovarian cancer, ^ breast and ovarian,
 * MESH:D061325_breast and, MESH:D061325_breast, and, MESH:D061325_^ breast and
 * ovarian, breast and ovarian, MESH:D061325_ovarian cancer $, ^ breast and
 * ovarian cancer, MESH:D061325_and ovarian cancer $, breast and,
 * MESH:D061325_cancer, MESH:D061325_and, MESH:D061325_^ breast and,
 * MESH:D061325_and ovarian cancer, ovarian cancer, and ovarian, ovarian cancer
 * $, ^ breast, cancer $, ^ breast and, MESH:D061325_^ breast and ovarian
 * cancer, MESH:D061325_breast and ovarian, MESH:D061325_cancer $,
 * MESH:D061325_breast and ovarian cancer $, MESH:D061325_ovarian, and ovarian
 * cancer, cancer, MESH:D061325_^ breast, breast, MESH:D061325_and ovarian,
 * MESH:D061325_ovarian cancer]
 * 
 * 
 * @author hterhors
 *
 *         Sep 22, 2016
 */
public class InternalMentionTokenTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	public static void main(String[] args) {
		final InternalMentionTokenTemplate template = new InternalMentionTokenTemplate();

		long t = System.currentTimeMillis();
		template.getTokenNgrams(new Concept("MESH:D061325", EEntityType.DISEASE), "breast and ovarian cancer").stream()
				.sorted().forEach(System.out::println);
		System.out.println(System.currentTimeMillis() - t);
	}

	private static Logger log = LogManager.getFormatterLogger(InternalMentionTokenTemplate.class.getName());

	static class Scope extends FactorScope {

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

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Vector featureVector = factor.getFeatureVector();

		final Concept concept = factor.getFactorScope().getConcept();

		final String mention = factor.getFactorScope().getText();

		final String cleanedMention = mention;

		final Set<String> tokenFeatures = getTokenNgrams(concept, cleanedMention);

		for (String tf : tokenFeatures) {
			featureVector.set(tf, true);
		}

	}

	private Set<String> getTokenNgrams(Concept concept, String cleanedMention) {

		final String cM = "^ " + cleanedMention + " $";

		final String[] tokens = cM.split(" ");

		final int maxNgramSize = tokens.length;

		final Set<String> features = new HashSet<>();

		features.add(cM);
		/*
		 * Skip features for unknown concepts.
		 */
		if (!concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
			features.add(concept.conceptID + "_" + cM);

		for (int ngram = 1; ngram < maxNgramSize; ngram++) {
			for (int i = 0; i < maxNgramSize - 1; i++) {

				/*
				 * Do not include start symbol.
				 */
				if (i + ngram == 1)
					continue;

				/*
				 * Break if size exceeds token length
				 */
				if (i + ngram > maxNgramSize)
					break;

				StringBuffer fBuffer = new StringBuffer();

				for (int t = i; t < i + ngram; t++) {
					fBuffer.append(tokens[t]).append(" ");
				}
				final String pureFeatureName = fBuffer.toString().trim();

				features.add(pureFeatureName);

				/*
				 * Skip features for unknown concepts.
				 */
				if (concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
					continue;

				features.add(concept.conceptID + "_" + pureFeatureName);
			}
		}

		return features;
	}

}

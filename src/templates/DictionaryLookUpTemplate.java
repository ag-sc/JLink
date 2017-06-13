package templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import sampler.MultipleTokenBoundaryExplorer;
import templates.DictionaryLookUpTemplate.Scope;
import util.StringUtils;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class DictionaryLookUpTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(DictionaryLookUpTemplate.class.getName());

	transient private CollectiveDictionary dict;

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

		public boolean isSynonymTransformed() {
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

	public DictionaryLookUpTemplate() {
		dict = CollectiveDictionaryFactory.getInstance();
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

		// log.debug("%s: Features for entity %s (\"%s\"): %s",
		// this.getClass().getSimpleName(), entity.getID(),
		// entity.getText(), featureVector);

		final Concept concept = factor.getFactorScope().getConcept();
		final String surfaceForm = factor.getFactorScope().getText();
		// final String noVowalsSurfaceForm =
		// StringUtil.removeVowels(surfaceForm);

		final boolean containsSynonym = factor.getFactorScope().isSynonymTransformed();

		final boolean isInDict = dict.containsSurfaceForm(surfaceForm);
		// final boolean isInNoVowelsDict =
		// dict.containsNoVowelsSurfaceForm(noVowalsSurfaceForm);

		// final boolean isAbbreviation =
		// StringUtil.isAbbreviation(surfaceForm);

		final String sortedSurfaceForm = StringUtils.sortTokens(surfaceForm);
		final boolean sortedIsInDict = dict.containsSortedSurfaceForm(sortedSurfaceForm);

		if (containsSynonym) {

			featureVector.set("ANNOTAION_IN_DICT_SYNONYM", isInDict);
			// featureVector.set("ANNOTAION_IN_DICT_SYNONYM_NO_VOWELS",
			// isInNoVowelsDict);
			// else if (sortedIsInDict)
			// featureVector.set("ABBREVIATION_ANNOTAION_IN_DICT",
			// sortedIsInDict);
		} else {
			featureVector.set("SORTED_ANNOTAION_IN_DICT", sortedIsInDict);
			featureVector.set("ANNOTAION_IN_DICT", isInDict);
			// featureVector.set("ANNOTAION_IN_DICT_NO_VOWELS",
			// isInNoVowelsDict);
		}
		// featureVector.set("ANNOTAION_IS_NOT_IN_DICT", !isInDict);

		// System.out.println("surfaceForm = " + surfaceForm + " isInDict = " +
		// isInDict);
		if (concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
			return;

		/*
		 * Do not use this feature for abbreviations.
		 */
		// if (isAbbreviation)
		// return;

		final boolean sortedSurfaceFormMatchesConcept = dict
				.sortedNormalizedSurfaceFormMatchesConcept(sortedSurfaceForm, factor.getFactorScope().getConcept());

		final boolean surfaceFormMatchesConcept = dict.normalizedSurfaceFormMatchesConcept(surfaceForm, concept);
		// final boolean noVowelsSurfaceFormMatchesConcept = dict
		// .noVowelsNormalizedSurfaceFormMatchesConcept(noVowalsSurfaceForm,
		// concept);

		// featureVector.set("ANNOTAION_DOES_NOT_MATCH_CONCEPT",
		// !surfaceFormMatchesConcept);
		if (containsSynonym) {
			featureVector.set("ANNOTAION_MATCHES_CONCEPT_SYNONYM", surfaceFormMatchesConcept);
			// featureVector.set("ANNOTAION_MATCHES_CONCEPT_SYNONYM_NO_VOWELS",
			// (isInNoVowelsDict && noVowelsSurfaceFormMatchesConcept));
			// else if (isAbbreviation)
			// featureVector.set("ABBREVIATION_ANNOTAION_MATCHES_CONCEPT",
			// (isInDict
			// && surfaceFormMatchesConcept));
		} else {
			featureVector.set("SORTED_ANNOTAION_MATCHES_CONCEPT", sortedSurfaceFormMatchesConcept);
			featureVector.set("ANNOTAION_MATCHES_CONCEPT", surfaceFormMatchesConcept);
			// featureVector.set("ANNOTAION_MATCHES_CONCEPT_NO_VOWELS",
			// (isInNoVowelsDict && noVowelsSurfaceFormMatchesConcept));
		}
		// System.out.println("surfaceForm = " + surfaceForm + "
		// surfaceFormMatchesConcept = " + surfaceFormMatchesConcept
		// + ", concept = " + concept);

	}

}

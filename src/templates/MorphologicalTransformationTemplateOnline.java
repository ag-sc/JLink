package templates;

import static templates.helper.DictLookUpHelper.extractCharRules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import metric.LevenShteinSimilarities;
import sampler.MultipleTokenBoundaryExplorer;
import templates.MorphologicalTransformationTemplateOnline.Scope;
import templates.container.CharacterRule;
import tokenization.Tokenizer;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

/**
 * The morphological transformations are now precomputed and used during
 * sampling.
 * 
 * @see MorphologicalTransformationTemplate.
 */
@Deprecated
public class MorphologicalTransformationTemplateOnline extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static final double SIMILARITY_TRESHOLD = 0.9;

	public static final int MAX_DISTANCE = 3;

	private static Logger log = LogManager
			.getFormatterLogger(MorphologicalTransformationTemplateOnline.class.getName());

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

	private CollectiveDictionary dict;

	private Map<Integer, Set<String>> distributedDict;

	public MorphologicalTransformationTemplateOnline() {
		System.out.print("Prepare Morphological Transformation Template...");
		dict = CollectiveDictionaryFactory.getInstance();

		buildDistributedDictionary();

		// buildKnownRules();
		System.out.println(" done!");
	}

	private void buildDistributedDictionary() {
		distributedDict = new HashMap<>();

		for (String entry : dict.getAllSurfaceForms()) {

			int l = entry.length();

			distributedDict.putIfAbsent(l, new HashSet<>());
			distributedDict.get(l).add(entry);

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

		final String cleanedMention = Tokenizer.getTokenizedForm(factor.getFactorScope().getText());

		final Concept concept = factor.getFactorScope().getConcept();

		Set<CharacterRule> rules = extractCharacterRules(cleanedMention, concept);

		for (CharacterRule characterRule : rules) {
			featureVector.set(characterRule.forwardRule, true);

			// if (knownRules.contains(characterRule.forwardRule)) {
			// featureVector.set("KNOWN_TRANSFORMTION_RULE", true);
			// } else {
			// featureVector.set("UNKOWN_TRANSFORMATION_RULE", true);
			// }

		}

		/*
		 * 
		 * 
		 * WITH INCLUDED FEATURE : TESTT ON PURPUR WITHOUT TEST ON PSINK
		 * 
		 * 
		 */
		/*
		 * If the concept was unk the surface form can not matches the id.
		 */
		// if (concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
		// return;

		/*
		 * If rules is NOT empty && the concept was not unknown, we found a rule
		 * which matches the to the id.
		 */
		// if (rules.size() == 0)
		// return;
		//
		// featureVector.set("APPLYING_RULE_MATCHES_ID", true);
	}

	private Set<CharacterRule> extractCharacterRules(String cleanedMention, final Concept concept) {

		Set<CharacterRule> rules = new HashSet<>();

		Set<String> dictionaryEntries;
		if (concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT)) {
			dictionaryEntries = new HashSet<>();
			int l = cleanedMention.length();
			for (int i = l - MAX_DISTANCE; i < l + MAX_DISTANCE; i++) {
				dictionaryEntries.addAll(distributedDict.getOrDefault(i, new HashSet<>()));
			}
		} else {
			dictionaryEntries = dict.getMentionsForConcept(concept);
		}

		dictionaryEntries.parallelStream().forEach(dictionaryEntry -> {

			CharacterRule rule = extractMorphologicalRule(cleanedMention, dictionaryEntry);

			if (rule != null)
				rules.add(rule);
		});

		return rules;
	}

	public static CharacterRule extractMorphologicalRule(String mention, String dictionaryEntry) {

		final double lengthDistance = Math.abs(mention.length() - dictionaryEntry.length());

		if (lengthDistance > MAX_DISTANCE)
			return null;

		final boolean matchesLengthCondition = Math.pow(
				1 - Math.pow(lengthDistance / Math.max(dictionaryEntry.length(), mention.length()), 2),
				2) >= SIMILARITY_TRESHOLD;

		if (!matchesLengthCondition)
			return null;

		final double levenshteinSimilarity = LevenShteinSimilarities.weightedLevenshteinSimilarity(mention,
				dictionaryEntry, MAX_DISTANCE);

		if (levenshteinSimilarity < SIMILARITY_TRESHOLD) {
			return null;
		}

		final CharacterRule r = extractCharRules(mention, dictionaryEntry, MAX_DISTANCE);

		return r;

	}

}

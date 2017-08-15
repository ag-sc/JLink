package sampler;

import static templates.helper.DictLookUpHelper.extractCharRules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import dictionary.Concept;
import dictionary.subdicts.ISubDictionary;
import metric.LevenShteinSimilarities;
import sampling.Explorer;
import templates.AbstractTemplate;
import templates.MorphologicalTransformationTemplate;
import templates.SyntacticTransformationTemplate;
import templates.container.CharacterRule;
import tokenization.Tokenizer;
import util.StringUtils;
import variables.JLinkState;
import variables.EEntityType;
import variables.EntityAnnotation;
import variables.Token;

public class MultipleTokenBoundaryExplorer implements Explorer<JLinkState> {

	public static final Concept UNK_CONCEPT = new Concept("<UNK>", EEntityType.UNK);

	private static final double SIMILARITY_TRESHOLD = 0.9;

	public static final int MAX_DISTANCE = 3;

	private static final int CONTEXT_LENGTH = 3;

	private static Logger log = LogManager.getFormatterLogger(MultipleTokenBoundaryExplorer.class.getName());
	private static Map<String, List<SynonymTuple>> syns = new HashMap<>();

	private boolean includeSynonyms = false;
	private boolean includeMorphs = false;

	private CollectiveDictionary dict;
	private Map<Integer, Set<String>> distributedDict;

	private Set<String> knownRules;

	private Map<String, Set<CharacterRule>> cache = new HashMap<>();
	private PosTagger posTagger;

	private MultipleTokenBoundaryExplorer(final boolean includeSynonyms, final boolean includeMorphs) {
		this.includeSynonyms = includeSynonyms;
		this.includeMorphs = includeMorphs;

		if (includeSynonyms) {
			posTagger = new PosTagger();
		}

		dict = CollectiveDictionaryFactory.getInstance();
		buildDistributedDictionary();
		try {
			if (includeSynonyms) {
				Map<String, Map<String, Double>> tmp = ExtractSynonymsAndRules.generateSynonymTokens();
				for (Entry<String, Map<String, Double>> syn : tmp.entrySet()) {

					/*
					 * If the word is not a noun remove it from the synonym
					 * list.
					 */

					if (!posTagger.wordIsNONAdjective(syn.getKey())) {
						continue;
					}

					/*
					 * Put itself with highest score
					 */
					// syn.getValue().put(syn.getKey(), 1d);

					syns.put(syn.getKey(),
							syn.getValue().entrySet().stream().map(e -> new SynonymTuple(e.getKey(), e.getValue()))
									.sorted().collect(Collectors.toList()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// syns.entrySet().forEach(System.out::println);
		buildPairwiseKnownRules();

	}

	public MultipleTokenBoundaryExplorer(Map<Class<? extends AbstractTemplate<?, ?, ?>>, Boolean> setting) {
		this(setting.get(SyntacticTransformationTemplate.class),
				setting.get(MorphologicalTransformationTemplate.class));
	}

	private void buildPairwiseKnownRules() {
		knownRules = new HashSet<>();

		for (ISubDictionary subDict : dict.getDictionaries()) {
			for (Entry<Concept, Set<String>> subDictEntry : subDict.getConceptBasedDictionary().entrySet()) {

				for (String string1 : subDictEntry.getValue()) {
					for (String string2 : subDictEntry.getValue()) {

						if (string1.equals(string2))
							continue;

						Set<CharacterRule> contextBasedRules = extractMorphologicalRules(string1, string2);
						if (contextBasedRules != null) {
							for (CharacterRule characterRule : contextBasedRules) {
								knownRules.add(characterRule.forwardRule);
							}
						}
					}
				}

			}
		}
	}

	private void buildDistributedDictionary() {
		distributedDict = new HashMap<>();

		for (String entry : dict.getAllSurfaceForms()) {

			int l = entry.length();

			distributedDict.putIfAbsent(l, new HashSet<>());
			distributedDict.get(l).add(entry);

		}
	}

	public List<JLinkState> getNextStates(JLinkState previousState) {

		List<JLinkState> generatedStates = new ArrayList<JLinkState>();

		List<Token> tokens = previousState.getDocument().getTokens();
		final int maxTokenPerAnnotation = 10;

		/*
		 * For all 10 ,9 ,8,7 ....
		 */
		for (int tokenPerAnnotation = maxTokenPerAnnotation; tokenPerAnnotation >= 1; tokenPerAnnotation--) {

			/*
			 * In 10 for all tokens in document T0-T9, T1-T10, ...
			 */
			for (int i = 0; i <= tokens.size() - tokenPerAnnotation; i++) {

				final String firstToken = Tokenizer.getTokenizedForm(tokens.get(i).getText());

				final boolean isAbbreviation;

				if (tokenPerAnnotation == 1) {
					if (firstToken.length() == 1) {
						continue;
					}

					isAbbreviation = StringUtils.isAbbreviation(firstToken);
				} else {
					isAbbreviation = false;
				}

				if (!isAbbreviation)
					if (firstToken.isEmpty() || !dict.containsToken(firstToken)) {
						continue;
					}

				if (tokenPerAnnotation > 1) {

					final String lastToken = Tokenizer
							.getTokenizedForm(tokens.get(i + tokenPerAnnotation - 1).getText());

					if (lastToken.isEmpty() || !dict.containsToken(lastToken)) {
						continue;
					}

				}

				boolean assign = true;
				/*
				 * Check each token in T0-T9
				 */
				for (int nextTokenIndex = 0; nextTokenIndex < tokenPerAnnotation; nextTokenIndex++) {

					assign &= !previousState.tokenHasAnnotation(tokens.get(i + nextTokenIndex));
					if (!assign)
						break;

					assign &= !tokens.get(i + nextTokenIndex).getText().equals(".");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals(",");

					if (!assign) {
						break;
					}

					assign &= !tokens.get(i + nextTokenIndex).getText().equals("%");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals("&");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals("+");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals(":");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals(";");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals("<");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals(">");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals("=");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals("?");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals("!");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals("[");
					assign &= !tokens.get(i + nextTokenIndex).getText().equals("]");

					if (!assign)
						break;
				}

				if (assign) {

					/*
					 * Add Identity
					 */
					JLinkState generatedState = new JLinkState(previousState);

					final int charStartIndex = tokens.get(i).getFrom();
					final int charEndIndex = tokens.get(i + tokenPerAnnotation - 1).getTo();

					final String originalText = previousState.getDocument().getContent().substring(charStartIndex,
							charEndIndex);

					/**
					 * TODO Check token index from first token start - > to last
					 * token end is the char position in the document.
					 */
					EntityAnnotation tokenAnnotation = new EntityAnnotation(generatedState, UNK_CONCEPT, originalText,
							tokens.get(i).getIndex(), tokens.get(i).getIndex() + tokenPerAnnotation, charStartIndex,
							charEndIndex);
					/*
					 * TODO: CHECK IF GOOD: IDentity is added in syns
					 * 
					 */
					generatedState.addEntity(tokenAnnotation);
					generatedStates.add(generatedState);

					// Assign new entity to empty tokens
					/*
					 * Add synonyms only if there is more then one token
					 */
					if (includeSynonyms) {
						if (tokenPerAnnotation > 1) {
							for (int nextTokenIndex = 0; nextTokenIndex < tokenPerAnnotation; nextTokenIndex++) {

								final String key = Tokenizer.getTokenizedForm(tokens.get(i + nextTokenIndex).getText());
								if (!syns.containsKey(key))
									continue;
								for (SynonymTuple s : syns.get(key)) {

									String synonym = s.token;

									// final double connectivity = s.score;
									// double backConnectivity = 0;
									//
									// if (syns.containsKey(synonym)) {
									//
									// for (SynonymTuple word :
									// syns.get(synonym)) {
									// if (word.token.equals(key)) {
									// backConnectivity = word.score;
									// break;
									// }
									// }
									// }
									//
									// final double synonymConnectivity =
									// (connectivity + backConnectivity) / 2;
									/*
									 * TODO: score is not needed if features are
									 * binary. Create Flag
									 */
									final double synonymConnectivity = -1;

									JLinkState gs = new JLinkState(previousState);

									EntityAnnotation tokenAnnotationSyn = new EntityAnnotation(gs, UNK_CONCEPT,
											tokens.get(i).getIndex(), tokens.get(i).getIndex() + tokenPerAnnotation,
											nextTokenIndex, synonym, synonymConnectivity, key);

									gs.addEntity(tokenAnnotationSyn);
									generatedStates.add(gs);
								}
							}
						}
					}

					if (includeMorphs) {

						/*
						 * Do not apply morphological rules on abbreviations.
						 */
						if (isAbbreviation)
							continue;

						final String text = tokenAnnotation.getText();

						boolean includeMorphologicalTransformations = !dict.containsSurfaceForm(text);

						if (includeMorphologicalTransformations) {

							Set<CharacterRule> rules;

							if (cache.containsKey(text)) {
								rules = cache.get(text);
							} else {
								rules = extractCharacterRules(text);
								cache.put(text, rules);
							}

							for (CharacterRule characterRule : rules) {
								JLinkState gs = new JLinkState(previousState);

								EntityAnnotation tokenAnnotationMorph = new EntityAnnotation(gs, UNK_CONCEPT,
										tokens.get(i).getIndex(), tokens.get(i).getIndex() + tokenPerAnnotation,
										characterRule.toString, characterRule.forwardRule);

								gs.addEntity(tokenAnnotationMorph);
								generatedStates.add(gs);
							}
						}
					}

				}
			}
		}
		Collections.shuffle(generatedStates);
		return generatedStates;
	}

	public Set<CharacterRule> extractCharacterRules(final String text) {
		Set<CharacterRule> rules = new HashSet<>();

		int l = text.length();
		for (int len = l - MAX_DISTANCE; len < l + MAX_DISTANCE; len++) {
			if (distributedDict.containsKey(len)) {
				rules.addAll(distributedDict.get(len).parallelStream().map(dictionaryEntry -> {
					Set<CharacterRule> contextBasedRules = extractMorphologicalRules(text, dictionaryEntry);

					if (contextBasedRules != null) {
						for (CharacterRule characterRule : contextBasedRules) {
							if (knownRules.contains(characterRule.forwardRule)) {
								return characterRule;
							}
						}
					}
					return null;
				}).filter(x -> x != null).collect(Collectors.toSet()));
			}
		}
		return rules;
	}

	private Set<CharacterRule> extractMorphologicalRules(String mention, String dictionaryEntry) {

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

		final Set<CharacterRule> r = extractCharRules(mention, dictionaryEntry, MAX_DISTANCE, CONTEXT_LENGTH);

		return r;

	}
}

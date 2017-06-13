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
import templates.TokenContextTemplate.Scope;
import tokenization.Tokenizer;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;
import variables.Token;

public class TokenContextTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static final String DISEASE_PLACEHOLDER = "<DISEASE>";
	private static Logger log = LogManager.getFormatterLogger(TokenContextTemplate.class.getName());

	static class Scope extends FactorScope {

		private EntityAnnotation entityType;
		private List<Token> tokens;

		public Scope(AbstractTemplate<?, ?, Scope> template, List<Token> tokens, EntityAnnotation entityType) {
			super(template, tokens, entityType);
			this.entityType = entityType;
			this.tokens = tokens;
		}

		public EntityAnnotation getEntityType() {
			return entityType;
		}

		public List<Token> getTokens() {
			return tokens;
		}

		public void setTokens(List<Token> tokens) {
			this.tokens = tokens;
		}

		public void setEntityType(EntityAnnotation entityType) {
			this.entityType = entityType;
		}

		@Override
		public String toString() {
			return "Scope [entityType=" + entityType + ", tokens=" + tokens + "]";
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		for (EntityAnnotation entity : state.getEntities()) {
			factors.add(new Scope(this, state.getInstance().getTokens(), entity));
		}
		return factors;
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Vector featureVector = factor.getFeatureVector();

		final Set<String> contextFeatures = getContextForToken(factor.getFactorScope().getTokens(),
				factor.getFactorScope().getEntityType(), true);

		for (String cf : contextFeatures) {
			featureVector.set(cf, true);
		}

	}

	private Set<String> getContextForToken(List<Token> tokens, EntityAnnotation entity, final boolean tokenizeContext) {

		final Set<String> contextFeatures = new HashSet<>();

		int pre = entity.getBeginTokenIndex();
		int post = entity.getEndTokenIndex();

		final List<String> leftContext = extractLeftContext(tokens, pre, tokenizeContext);

		final List<String> rightContext = extractRightContext(tokens, post, tokenizeContext);

		contextFeatures.addAll(getContextFeatures(leftContext, rightContext));

		final Concept concept = entity.getType();
		/*
		 * Skip features for unknown concepts.
		 */
		if (concept.equals(MultipleTokenBoundaryExplorer.UNK_CONCEPT))
			return contextFeatures;

		contextFeatures.addAll(conceptDependendContextFeatures(concept.conceptID, leftContext, rightContext));

		return contextFeatures;
	}

	private List<String> extractLeftContext(List<Token> tokens, int pre, final boolean tokenizeContext) {
		final List<String> leftContext = new ArrayList<>();

		// 4
		for (int i = 1; i < 4; i++) {
			if (pre - i >= 0) {

				String leftTokens = tokens.get(pre - i).getText();

				if (tokenizeContext)
					leftTokens = Tokenizer.getTokenizedForm(leftTokens).trim();

				if (leftTokens.isEmpty())
					continue;

				leftContext.add(leftTokens);

			} else {
				break;
			}
		}
		return leftContext;
	}

	private List<String> extractRightContext(List<Token> tokens, int post, final boolean tokenizeContext) {
		final List<String> rightContext = new ArrayList<>();

		// 3
		for (int i = 0; i < 3; i++) {
			if (post + i < tokens.size()) {

				String rightTokens = tokens.get(post + i).getText();

				if (tokenizeContext)
					rightTokens = Tokenizer.getTokenizedForm(rightTokens).trim();

				if (rightTokens.isEmpty())
					continue;

				rightContext.add(rightTokens);

			} else {
				break;
			}
		}
		return rightContext;

	}

	private Set<String> getContextFeatures(final List<String> leftContext, final List<String> rightContext) {

		final Set<String> contextFeatures = new HashSet<>();

		String lCs = "";
		String rCs = "";

		for (String lC : leftContext) {
			rCs = "";
			lCs = lC + " " + lCs;
			contextFeatures.add((lCs + DISEASE_PLACEHOLDER + rCs).trim());
			for (String rC : rightContext) {
				rCs += " " + rC;
				contextFeatures.add((lCs + DISEASE_PLACEHOLDER + rCs).trim());
			}

		}
		rCs = "";
		lCs = "";

		for (String rC : rightContext) {
			lCs = "";
			rCs += " " + rC;
			contextFeatures.add((lCs + DISEASE_PLACEHOLDER + rCs).trim());
			for (String lC : leftContext) {
				lCs = lC + " " + lCs;
				contextFeatures.add((lCs + DISEASE_PLACEHOLDER + rCs).trim());
			}

		}
		return contextFeatures;
	}

	private Set<String> conceptDependendContextFeatures(String diseaseID, final List<String> leftContext,
			final List<String> rightContext) {
		final Set<String> contextFeatures = new HashSet<>();

		String rCs = "";
		String lCs = "";
		for (String lC : leftContext) {
			rCs = "";
			lCs = lC + " " + lCs;
			contextFeatures.add(diseaseID + "_" + (lCs + rCs).trim());
			contextFeatures.add(diseaseID + "_" + (lCs + DISEASE_PLACEHOLDER + rCs).trim());
			for (String rC : rightContext) {
				rCs += " " + rC;
				contextFeatures.add(diseaseID + "_" + (lCs + DISEASE_PLACEHOLDER + rCs).trim());
				contextFeatures.add(diseaseID + "_" + (lCs + rCs).trim());
			}

		}
		rCs = "";
		lCs = "";

		for (String rC : rightContext) {
			lCs = "";
			rCs += " " + rC;
			contextFeatures.add(diseaseID + "_" + (lCs + DISEASE_PLACEHOLDER + rCs).trim());
			contextFeatures.add(diseaseID + "_" + (lCs + rCs).trim());
			for (String lC : leftContext) {
				lCs = lC + " " + lCs;
				contextFeatures.add(diseaseID + "_" + (lCs + DISEASE_PLACEHOLDER + rCs).trim());
				contextFeatures.add(diseaseID + "_" + (lCs + rCs).trim());
			}

		}
		return contextFeatures;
	}
}

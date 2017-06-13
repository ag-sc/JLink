package templates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dictionary.CollectiveDictionary;
import dictionary.CollectiveDictionaryFactory;
import factors.Factor;
import factors.FactorScope;
import learning.Vector;
import templates.NumberOfTokensTemplate.Scope;
import tokenization.Tokenizer;
import variables.JLinkState;
import variables.EntityAnnotation;
import variables.LabeledJlinkDocument;

public class NumberOfTokensTemplate extends AbstractTemplate<LabeledJlinkDocument, JLinkState, Scope>
		implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(NumberOfTokensTemplate.class.getName());

	private transient CollectiveDictionary dict;

	public NumberOfTokensTemplate() {
		dict = CollectiveDictionaryFactory.getInstance();
	}

	static class Scope extends FactorScope {

		private String text;

		public Scope(AbstractTemplate<?, ?, Scope> template, String text) {
			super(template, text);
			this.text = text;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "Scope [text=" + text + "]";
		}

	}

	@Override
	public List<Scope> generateFactorScopes(JLinkState state) {
		List<Scope> factors = new ArrayList<>();
		for (EntityAnnotation entity : state.getEntities()) {
			factors.add(new Scope(this, entity.getText()));
		}
		return factors;
	}

	@Override
	public void computeFactor(Factor<Scope> factor) {

		Vector featureVector = factor.getFeatureVector();

		final String[] tokens = Tokenizer.getTokenizedForm(factor.getFactorScope().getText()).split(" ");

		final int numberOfTokens = tokens.length;

		for (int i = 1; i <= numberOfTokens; i++) {
			featureVector.set("NUMBER_OF_TOKENS_>= " + i, true);
		}

		// if (numberOfTokens == 1)
		// return;
		//
		// int countTokensInDict = 0;
		// for (String token : tokens) {
		// countTokensInDict += dict.containsToken(token) ? 1 : 0;
		// }
		//
		// if (countTokensInDict == numberOfTokens)
		// featureVector.set("ALL_TOKENS_IN_DICT", true);

	}

}

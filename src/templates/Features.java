package templates;

import java.util.List;

import variables.JLinkState;
import variables.EntityAnnotation;
import variables.Token;

public class Features {

	public static final String PUNCTUATION = "!\"#$%&'\\(\\)\\*\\+,-\\./:;<=>\\?@\\[\\]\\^_`{\\|}~";

	public static final TokenFeature ContainsDigit = new TokenFeature() {

		@Override
		public boolean apply(Token token) {
			return containsDigit(token.getText());
		}
	};
	public static final TokenFeature ContainsHyphen = new TokenFeature() {

		@Override
		public boolean apply(Token token) {
			return containsHyphen(token.getText());
		}
	};
	public static final TokenFeature ContainsGreek = new TokenFeature() {

		@Override
		public boolean apply(Token token) {
			return containsGreek(token.getText());
		}
	};

	public static final TokenFeature ContainsPunctuation = new TokenFeature() {

		@Override
		public boolean apply(Token token) {
			return containsPunctuation(token.getText());
		}
	};

	public static final TokenFeature StartsWithCapital = new TokenFeature() {

		@Override
		public boolean apply(Token token) {
			return startsWithUppercase(token.getText());
		}
	};
	public static final TokenFeature AllCapital = new TokenFeature() {

		@Override
		public boolean apply(Token token) {
			return isUppercase(token.getText());
		}
	};

	public static double b2d(boolean b) {
		return b ? 1 : 0;
	}

	private static boolean containsDigit(String text) {
		return contains(text, "\\d");
	}

	private static boolean containsHyphen(String text) {
		return contains(text, "-");
	}

	private static boolean containsGreek(String text) {
		// TODO Unicode pattern
		return contains(text, "\\p{InGreekExtended}");
	}

	private static boolean containsPunctuation(String text) {
		return contains(text, String.format("[%s]", PUNCTUATION));
	}

	private static boolean startsWithUppercase(String text) {
		return isUppercase(text.substring(0, 1));
	}

	private static boolean isUppercase(String text) {
		// TODO US-ASCII pattern
		return text.matches("[\\p{Upper}]+");
	}

	private static boolean contains(String text, String regex) {
		return text.matches(String.format(".*%s.*", regex));
	}

	public static Token getTokenRelativeToEntity(JLinkState state, EntityAnnotation e, int at) {
		int absolutPosition = -1;
		if (at < 0)
			absolutPosition = e.getBeginTokenIndex() + at;
		else if (at > 0)
			absolutPosition = e.getEndTokenIndex() + at - 1;

		if (absolutPosition < 0 || absolutPosition > state.getDocument().getTokens().size() - 1)
			return null;
		return state.getDocument().getTokens().get(absolutPosition);
	}
}

abstract class TokenFeature {
	public abstract boolean apply(Token token);

	public double all(List<Token> tokens) {
		for (Token token : tokens) {
			if (!apply(token))
				return 0;
		}
		return 1;
	}

	public double first(List<Token> tokens) {
		return Features.b2d(apply(tokens.get(0)));
	}

	public double last(List<Token> tokens) {
		return Features.b2d(apply(tokens.get(tokens.size() - 1)));
	}

	public double any(List<Token> tokens) {
		for (Token token : tokens) {
			if (apply(token))
				return 1;
		}
		return 0;
	}
}
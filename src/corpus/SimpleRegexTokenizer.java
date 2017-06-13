package corpus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import variables.Token;

public class SimpleRegexTokenizer {

	// private static Pattern pattern =
	// Pattern.compile("[a-zA-Z]+|\\d+|[^\\w\\s]");
	private static Pattern pattern = Pattern.compile("\\w+|[^\\w\\s]");

	public static void main(String[] args) {
		System.out.println(new SimpleRegexTokenizer().tokenize(Arrays.asList("C7-Complement")));
	}

	public List<Tokenization> tokenize(List<String> sentences) {
		List<Tokenization> tokenizations = new ArrayList<>();
		int accumulatedSentenceLength = 0;
		for (String sentence : sentences) {
			int index = 0;
			Matcher matcher = pattern.matcher(sentence);
			List<Token> tokens = new ArrayList<>();
			while (matcher.find()) {
				String text = matcher.group();
				int from = matcher.start();
				int to = matcher.end();
				tokens.add(new Token(index, from, to, text));
				index++;
			}
			Tokenization tokenization = new Tokenization(tokens, sentence, accumulatedSentenceLength);
			tokenizations.add(tokenization);
			accumulatedSentenceLength += sentence.length();
			// System.out.println(tokenization.originalSentence);
			// System.out.println(
			// tokenization.tokens.stream().reduce("", (s, t) -> s + " " +
			// t.getText(), (s, tt) -> s + tt));
		}
		return tokenizations;
	}

	public Tokenization tokenize(String sentence) {
		int accumulatedSentenceLength = 0;
		int index = 0;
		Matcher matcher = pattern.matcher(sentence);
		List<Token> tokens = new ArrayList<>();
		while (matcher.find()) {
			String text = matcher.group();
			int from = matcher.start();
			int to = matcher.end();
			tokens.add(new Token(index, from, to, text));
			index++;
		}
		Tokenization tokenization = new Tokenization(tokens, sentence, accumulatedSentenceLength);
		accumulatedSentenceLength += sentence.length();
		return tokenization;
	}

}
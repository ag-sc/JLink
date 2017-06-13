package tokenization;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import candidateretrieval.Stopwords;
import util.StringUtils;

public class Tokenizer {

	static public boolean REMOVE_STOPWORDS = false;
	static public boolean TO_LOWER_CASE_IF_NOT_UPPER_CASE = true;
	static public boolean ENABLE_TOKENIZATION_RULES = true;

	public static Pattern p1 = Pattern.compile("\\[[0-9]+\\]");
	public static Pattern p2 = Pattern.compile("(-|\\,|\\.|:)(?![0-9])|;|\"|\\?");
	public static Pattern p3 = Pattern.compile("'s|s'");
	public static Pattern p4 = Pattern.compile("\\[|\\]|\\(|\\)|\\{|\\}|'");
	public static Pattern p5 = Pattern.compile(" +|\t+");

	private static String bagOfWordsTokenizer(String text) {

		if (ENABLE_TOKENIZATION_RULES) {
			text = text.replaceAll(p1.pattern(), "");
			text = text.replaceAll(p2.pattern(), " ");
			text = text.replaceAll(p3.pattern(), "s");
			text = text.replaceAll(p4.pattern(), "");
			text = text.replaceAll(p5.pattern(), " ");
		}

		StringBuffer modifiedText = new StringBuffer();

		for (String token : text.split(" ")) {

			if (REMOVE_STOPWORDS && Stopwords.DISEASE_ENGLISH_STOP_WORDS.contains(token))
				continue;

			if (TO_LOWER_CASE_IF_NOT_UPPER_CASE) {
				token = StringUtils.toLowercaseIfNotUppercase(token);
			}

			modifiedText.append(token + " ");
		}

		text = modifiedText.toString().trim();

		return text;

	}

	static Map<Integer, String> numberMapping = new HashMap<>();
	static Map<String, String> numberMapping2 = new HashMap<>();

	static {
		numberMapping.put(1, "first");
		numberMapping.put(2, "second");
		numberMapping.put(3, "third");
		numberMapping.put(4, "fourth");
		numberMapping.put(5, "fifth");
		numberMapping.put(6, "sixth");
		numberMapping.put(7, "seventh");
		numberMapping.put(8, "eighth");
		numberMapping.put(9, "ninth");

		numberMapping2.put("1st", "first");
		numberMapping2.put("2nd", "second");
		numberMapping2.put("3rd", "third");
		numberMapping2.put("4th", "fourth");
		numberMapping2.put("5th", "fifth");
		numberMapping2.put("6th", "sixth");
		numberMapping2.put("7th", "seventh");
		numberMapping2.put("8th", "eighth");
		numberMapping2.put("9th", "ninth");
	}

	private static String numberReplacement(String token) {
		try {

			int i = Integer.parseInt(token);

			/*
			 * Success
			 */
			if (numberMapping.containsKey(i))
				token = numberMapping.get(i);

		} catch (Exception e) {
		}

		if (numberMapping2.containsKey(token))
			token = numberMapping2.get(token);

		return token;
	}

	public static Map<String, String> tokenizeMapping = new ConcurrentHashMap<String, String>();

	public static String getTokenizedForm(final String surfaceFormRepresentation) {

		String tokenizedAnnotationRepresentation;
		if (tokenizeMapping.containsKey(surfaceFormRepresentation.trim())) {
			tokenizedAnnotationRepresentation = tokenizeMapping.get(surfaceFormRepresentation.trim());
		} else {
			tokenizedAnnotationRepresentation = Tokenizer.bagOfWordsTokenizer(surfaceFormRepresentation);

			tokenizeMapping.put(surfaceFormRepresentation.trim(), tokenizedAnnotationRepresentation.trim());
		}
		return tokenizedAnnotationRepresentation.trim();
	}
}

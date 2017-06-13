package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtils {
	private static final int ABBREVIATION_MAX_LENGTH = 6;
	private static final int ABBREVIATION_MIN_LENGTH = 2;

	private static final String VOWELS_REG_EXP = "[AEIOUYaeiouy]*";

	public static void main(String[] args) {

		String text = "MI";

		System.out.println(isUpperCase(text));
		System.out.println(isAbbreviation(text));
		System.out.println(removeVowels(text));

	}

	public static String toLowercaseIfNotUppercase(String s) {

		if (!isUpperCase(s))
			return s.toLowerCase();

		return s;
	}

	public static boolean isUpperCase(String s) {
		return s.matches("[A-Z0-9]+");
	}

	public static boolean isAbbreviation(String cleanedAnnotation) {
		boolean isAbbreviation = cleanedAnnotation
				.matches("[A-Z0-9]{" + ABBREVIATION_MIN_LENGTH + "," + ABBREVIATION_MAX_LENGTH + "}");

		return isAbbreviation;

	}

	public static String sortTokens(String surfaceForm) {

		StringBuffer bf = new StringBuffer();

		final List<String> d = new ArrayList<>(Arrays.asList(surfaceForm.split(" ")));

		Collections.sort(d);

		for (String token : d) {
			bf.append(token);
			bf.append(" ");
		}

		return bf.toString().trim();
	}

	public static String removeVowels(String text) {
		return text.replaceAll(VOWELS_REG_EXP, "");
	}

}

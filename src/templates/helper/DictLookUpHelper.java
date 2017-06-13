package templates.helper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import templates.container.CharacterRule;
import templates.container.TokenRule;

public class DictLookUpHelper {

	public static void main(String[] args) {
		// String a = "ventricular fibrillation".toLowerCase();
		// String b = "Genetic basis and molecular mechanism for idiopathic
		// ventricular fibrillation. Ventricular fibrillation causes more than
		// 300, 000 sudden deaths each year in the USA alone. In approximately
		// 5-12% of these cases, there are no demonstrable cardiac or
		// non-cardiac causes to account for the episode, which is therefore
		// classified as idiopathic ventricular fibrillation (IVF). A distinct
		// group of IVF patients has been found to present with a characteristic
		// electrocardiographic pattern. Because of the small size of most
		// pedigrees and the high incidence of sudden death, however, molecular
		// genetic studies of IVF have not yet been done. Because IVF causes
		// cardiac rhythm disturbance, we investigated whether malfunction of
		// ion channels could cause the disorder by studying mutations in the
		// cardiac sodium channel gene SCN5A. We have now identified a missense
		// mutation, a splice-donor mutation, and a frameshift mutation in the
		// coding region of SCN5A in three IVF families. We show that sodium
		// channels with the missense mutation recover from inactivation more
		// rapidly than normal and that the frameshift mutation causes the
		// sodium channel to be non-functional. Our results indicate that
		// mutations in cardiac ion-channel genes contribute to the risk of
		// developing IVF."
		// .toLowerCase();
		// System.out.println("countMatches: " + countMatches(a, b));

		/*
		 * TODO: FIXME:
		 */
		// System.out.println(extractCharRules("myotania congenita", "myotonia
		// congenita", 3));
		extractCharRules("leukeydgsdhmia", "leukemia", 3, 3).forEach(System.out::println);

		// System.out.println("extractCharDifferencesFast = "
		// + extractTokenDifferences("foreign body migrations", "ischemic
		// cardiac infections 9", 1));
		// System.out.println("extractCharDifferencesFast = "
		// + extractMissingToken("underlying nail abnormality", "cardiac
		// abnormality", 1));
		//
		// System.out.println("extractCharDifferencesFast = " +
		// extractMissingToken("nail abnormality", "abnormality", 1));
		//
		// System.out.println("extractCharDifferencesFast = "
		// + extractMissingToken("underlying nail abnormality", "underlying
		// cardiac abnormality", 1));
		// System.out.println("extractCharDifferencesFast = "
		// + extractMissingToken("underlying cardiac", "underlying cardiac
		// abnormality", 1));
		// System.out.println("extractCharDifferencesFast = "
		// + extractMissingToken("underlying cardiac abnormality", "underlying
		// cardiac", 1));

	}

	public static int countMatches(final String sub, final String str) {
		int lastIndex = 0;
		int count = 0;

		while (lastIndex != -1) {

			lastIndex = str.indexOf(sub, lastIndex);

			if (lastIndex != -1) {
				count++;
				lastIndex += sub.length();
			}
		}
		return count;
	}

	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static Set<CharacterRule> extractCharRules(String fromString, String toString,
			final int MAX_LEVENSHTEIN_DIST, final int maxContextCharLength) {

		Set<CharacterRule> rules = new HashSet<>();

		R a;
		R b;
		a = extractCharDifferences(fromString, toString, MAX_LEVENSHTEIN_DIST);
		if (a == null)
			return null;

		b = extractCharDifferences(toString, fromString, MAX_LEVENSHTEIN_DIST);

		if (b == null || a.equals(b))
			return null;

		for (int contextLength = 0; contextLength < maxContextCharLength; contextLength++) {
			rules.add(new CharacterRule(fromString, toString, a.errIndex, b.errIndex,
					a.getErrCharsLeftContext(contextLength), b.getErrCharsLeftContext(contextLength)));
			rules.add(new CharacterRule(fromString, toString, a.errIndex, b.errIndex,
					a.getErrCharsRightContext(contextLength), b.getErrCharsRightContext(contextLength)));
			rules.add(new CharacterRule(fromString, toString, a.errIndex, b.errIndex,
					a.getErrCharsFullContext(contextLength), b.getErrCharsFullContext(contextLength)));
		}

		return rules;
	}

	public static CharacterRule extractCharRules(String fromString, String toString, final int MAX_LEVENSHTEIN_DIST) {
		R a;
		R b;
		a = extractCharDifferences(fromString, toString, MAX_LEVENSHTEIN_DIST);
		if (a == null)
			return null;

		b = extractCharDifferences(toString, fromString, MAX_LEVENSHTEIN_DIST);

		if (b == null || a.equals(b))
			return null;

		return new CharacterRule(fromString, toString, a.errIndex, b.errIndex, a.getErrChars(), b.getErrChars());

	}

	public static TokenRule extractTokenRule(String tokenizedAnnotationRepresentation, String string,
			final int MAX_TOKEN_DIFF) {
		try {

			String a;
			String b;
			a = extractTokenDifferences(tokenizedAnnotationRepresentation, string, MAX_TOKEN_DIFF);

			if (a == null)
				return null;

			b = extractTokenDifferences(string, tokenizedAnnotationRepresentation, MAX_TOKEN_DIFF);

			if (b == null || a.equals(b))
				return null;

			return new TokenRule(a, b);
		} catch (Exception e) {
			System.out.println(string + "<>" + tokenizedAnnotationRepresentation);

		}
		return null;
	}

	public static R extractCharDifferences(String string1, String string2, final int MAX_LEVENSHTEIN_DIST) {

		StringBuffer bf = new StringBuffer(string1);

		final char[] a = string1.toCharArray();
		final char[] b = string2.toCharArray();

		final int l = Math.min(a.length, b.length);

		int startIndex = 0;
		int stopIndex = bf.length();
		int tokenIndex = 0;

		for (int i = 0; i < l; i++) {
			if (a[i] == ' ')
				tokenIndex++;

			if (a[i] == b[i]) {
				startIndex++;
			} else {
				break;
			}
		}
		final int al = a.length - 1;
		final int bl = b.length - 1;

		for (int i = 0; i < l; i++) {
			if (a[al - i] == b[bl - i]) {
				stopIndex--;
			} else {
				break;
			}
		}

		if (Math.abs(startIndex - stopIndex) > MAX_LEVENSHTEIN_DIST)
			return null;

		if (stopIndex < startIndex)
			return null;

		return new R(tokenIndex, string1, startIndex, stopIndex);
	}

	static class R {
		final int errIndex;
		final int startIndex;
		final int stopIndex;
		final String word;

		public R(int errIndex, String word, int startIndex, int stopIndex) {
			this.word = word;
			this.errIndex = errIndex;
			this.startIndex = startIndex;
			this.stopIndex = stopIndex;
		}

		public String getErrChars() {
			return word.substring(startIndex, stopIndex);
		}

		public String getErrCharsLeftContext(int context) {
			return word.substring(Math.max(startIndex - context, 0), stopIndex);
		}

		public String getErrCharsRightContext(int context) {
			return word.substring(startIndex, Math.min(word.length(), stopIndex + context));
		}

		public String getErrCharsFullContext(int context) {
			return word.substring(Math.max(startIndex - context, 0), Math.min(word.length(), stopIndex + context));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + errIndex;
			result = prime * result + startIndex;
			result = prime * result + stopIndex;
			result = prime * result + ((word == null) ? 0 : word.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			R other = (R) obj;
			if (errIndex != other.errIndex)
				return false;
			if (startIndex != other.startIndex)
				return false;
			if (stopIndex != other.stopIndex)
				return false;
			if (word == null) {
				if (other.word != null)
					return false;
			} else if (!word.equals(other.word))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "R [errIndex=" + errIndex + ", startIndex=" + startIndex + ", stopIndex=" + stopIndex + ", word="
					+ word + "]";
		}

	}

	public static String extractTokenDifferences(String string1, String string2, final int MAX_TOKEN_DIFF) {

		final String[] a = string1.split(" ");
		final String[] b = string2.split(" ");

		final List<String> bf = Arrays.asList(a);

		final int l = Math.min(a.length, b.length);

		if (a.length == 1 || b.length == 1)
			return null;

		if (Math.abs(a.length - b.length) > MAX_TOKEN_DIFF)
			return null;

		int startIndex = 0;
		int stopIndex = bf.size();

		for (int i = 0; i < l; i++) {
			if (a[i].equals(b[i])) {
				startIndex++;
			} else {
				break;
			}
		}

		final int al = a.length - 1;
		final int bl = b.length - 1;

		for (int i = 0; i < l; i++) {
			if (a[al - i].equals(b[bl - i])) {
				stopIndex--;
			} else {
				break;
			}
		}

		if (startIndex == stopIndex || stopIndex > bf.size())
			return "";

		if (Math.abs(startIndex - stopIndex) > MAX_TOKEN_DIFF)
			return null;

		/*
		 * context
		 */
		// startIndex = Math.max(0, startIndex - 1);
		// stopIndex = Math.min(bf.size(), stopIndex + 1);

		return bf.subList(startIndex, stopIndex).stream().map(t -> " " + t.toString()).reduce("", String::concat)
				.trim();
	}

	public static String extractMissingToken(String goldString, String observedString, final int MAX_TOKEN_DIFF) {

		final String[] a = goldString.split(" ");
		final String[] b = observedString.split(" ");

		final List<String> bf = Arrays.asList(a);

		final int l = Math.min(a.length, b.length);

		if (a.length == 1)
			return null;

		if (a.length <= b.length)
			return null;

		if (Math.abs(a.length - b.length) > MAX_TOKEN_DIFF)
			return null;

		int startIndex = 0;
		int stopIndex = bf.size();

		for (int i = 0; i < l; i++) {
			if (a[i].equals(b[i])) {
				startIndex++;
			} else {
				break;
			}
		}

		final int al = a.length - 1;
		final int bl = b.length - 1;

		for (int i = 0; i < l; i++) {
			if (a[al - i].equals(b[bl - i])) {
				stopIndex--;
			} else {
				break;
			}
		}

		if (startIndex == stopIndex || stopIndex > bf.size())
			return "";

		if (Math.abs(startIndex - stopIndex) > MAX_TOKEN_DIFF)
			return null;

		/*
		 * context
		 */
		// startIndex = Math.max(0, startIndex - 1);
		// stopIndex = Math.min(bf.size(), stopIndex + 1);

		return bf.subList(startIndex, stopIndex).stream().map(t -> " " + t.toString()).reduce("", String::concat)
				.trim();
	}

}

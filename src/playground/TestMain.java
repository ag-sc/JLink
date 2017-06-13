package playground;

public class TestMain {

	public static void main(String[] args) {

		// String 1 = *autosomal dominant cerebellar ataxia type I*
		// String 2 = *autosomal dominant cerebellar ataxia type II*

		long t = System.nanoTime();
		System.out.println(extractDifferences("abce", "abcde"));
		System.out.println(extractDifferences("abcde", "abce"));
		long t2 = System.nanoTime();
		System.out.println(t2 - t);
		System.out.println();
		t = System.nanoTime();
		System.out.println(extractDifferences("abcde", "abcd"));
		System.out.println(extractDifferences("abcd", "abcde"));
		t2 = System.nanoTime();
		System.out.println(t2 - t);

		System.out.println();
		t = System.nanoTime();
		System.out.println(extractDifferences("bcde", "abcde"));
		System.out.println(extractDifferences("abcde", "bcde"));
		t2 = System.nanoTime();
		System.out.println(t2 - t);
		System.out.println();

		t = System.nanoTime();
		System.out.println(extractDifferences("abcfe", "abcde"));
		System.out.println(extractDifferences("abcde", "abcfe"));
		t2 = System.nanoTime();
		System.out.println(t2 - t);
		System.out.println();
	}

	private static String extractDifferences(String string1, String string2) {

		string1 = "*" + string1 + "*";
		string2 = "*" + string2 + "*";

		final char[] a = string1.toCharArray();
		final char[] b = string2.toCharArray();

		final int l = Math.min(a.length, b.length);

		int startindex = -1;
		int stopindex = -1;

		for (int i = 0; i < l; i++) {
			if (a[i] == b[i]) {
				continue;
			} else {
				startindex = i;
				break;
			}
		}

		startindex -= 1;

		if (startindex < 0) {
			startindex = 0;
		}

		final int al = a.length - 1;
		final int bl = b.length - 1;

		for (int i = 0; i < l; i++) {
			if (a[al - i] == b[bl - i]) {
				continue;
			} else {
				stopindex = i;
				break;
			}
		}

		stopindex = string1.length() - stopindex;
		stopindex = Math.min(string1.length(), stopindex + 1);

		String t = string1.substring(startindex + 1, stopindex - 1);

		if (!t.isEmpty()) {
			if (t.charAt(0) == '*') {
				t = t.substring(1);
			}
			if (t.charAt(t.length() - 1) == '*')
				t = t.substring(0, t.length() - 1);
		}
		return t;
	}

}

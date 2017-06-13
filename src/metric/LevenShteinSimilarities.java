package metric;

import java.io.IOException;
import java.util.Arrays;

public class LevenShteinSimilarities {

	public static void main(String[] args) throws IOException {
		// long t1 = System.nanoTime();
		// int j = levenshteinDistance("splokjhgfoiujzhgfcvbnkuzitgfbreast
		// cancerqwertzuiopüasdfghjklöyxcvbnm,",
		// "sdfgkhldtkzsxgnfjkuzsfvöiuebgögh<öbgdöäo<hgdändägijdpjhpoxbdshajsztks",
		// 5);
		// long t2 = System.nanoTime();
		// System.out.println(t2 - t1 + " j = " + j);

		// DiseaseDictionary fullDict =
		// DiseaseDictionaryFactory.getInstance(false, ETokenizationType.SIMPLE,
		// true, true,
		// false, false);
		// int i = Integer.MAX_VALUE;
		// long t1 = System.nanoTime();
		// int c = 90000;
		// for (String dictEntry1 : fullDict.surfaceFormToDiseaseID.keySet()) {
		// for (String dictEntry2 : fullDict.surfaceFormToDiseaseID.keySet()) {
		// i = Math.min(i, WordMetric.levenshteinDistance(dictEntry2,
		// dictEntry1, 4));
		// }
		// c--;
		// if (c == 0)
		// break;
		// }
		// long t2 = System.nanoTime();
		// System.out.println(t2 - t1 + "\t" + i);
		System.out.println(LevenShteinSimilarities.levenshteinSimilarity("ica", "mia", 5));
		System.out.println(LevenShteinSimilarities.weightedLevenshteinSimilarity("ica", "mia", 5));
		System.out.println(LevenShteinSimilarities.levenshteinSimilarity("ischemia myocardial", "ischemic myocardium", 5));
		System.out.println(LevenShteinSimilarities.weightedLevenshteinSimilarity("ischemia myocardial", "ischemic myocardium", 5));
	}

	/**
	 * (max - d) / max
	 * 
	 * threshold
	 * 
	 * @param a
	 * @param b
	 * @return
	 */

	public static double levenshteinSimilarity(final String a, final String b, final int threshold) {
		final int max = Math.max(a.length(), b.length());
		double d = levenshteinDistance(a, b, threshold);
		return (max - d) / max;
	}

	/**
	 * (1- (d/max)^2)^2
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double weightedLevenshteinSimilarity(final String a, final String b, final int threshold) {
		final int max = Math.max(a.length(), b.length());
		double d = levenshteinDistance(a, b, threshold);
		return Math.pow(1 - Math.pow((d / max), 2), 2);
	}

	/*
	 * The following code has been copied from the Apached Commons Lang library.
	 */

	/*
	 * Licensed to the Apache Software Foundation (ASF) under one or more
	 * contributor license agreements. See the NOTICE file distributed with this
	 * work for additional information regarding copyright ownership. The ASF
	 * licenses this file to You under the Apache License, Version 2.0 (the
	 * "License"); you may not use this file except in compliance with the
	 * License. You may obtain a copy of the License at
	 * 
	 * http://www.apache.org/licenses/LICENSE-2.0
	 * 
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations
	 * under the License.
	 */
	/**
	 * <p>
	 * Find the Levenshtein distance between two Strings.
	 * </p>
	 *
	 * <p>
	 * This is the number of changes needed to change one String into another,
	 * where each change is a single character modification (deletion, insertion
	 * or substitution).
	 * </p>
	 *
	 * <p>
	 * The previous implementation of the Levenshtein distance algorithm was
	 * from
	 * <a href="http://www.merriampark.com/ld.htm">http://www.merriampark.com
	 * /ld.htm</a>
	 * </p>
	 *
	 * <p>
	 * Chas Emerick has written an implementation in Java, which avoids an
	 * OutOfMemoryError which can occur when my Java implementation is used with
	 * very large strings.<br>
	 * This implementation of the Levenshtein distance algorithm is from
	 * <a href="http://www.merriampark.com/ldjava.htm">http://www.merriampark.
	 * com/ ldjava.htm</a>
	 * </p>
	 *
	 * <pre>
	 * StringUtils.getLevenshteinDistance(null, *)             = IllegalArgumentException
	 * StringUtils.getLevenshteinDistance(*, null)             = IllegalArgumentException
	 * StringUtils.getLevenshteinDistance("","")               = 0
	 * StringUtils.getLevenshteinDistance("","a")              = 1
	 * StringUtils.getLevenshteinDistance("aaapppp", "")       = 7
	 * StringUtils.getLevenshteinDistance("frog", "fog")       = 1
	 * StringUtils.getLevenshteinDistance("fly", "ant")        = 3
	 * StringUtils.getLevenshteinDistance("elephant", "hippo") = 7
	 * StringUtils.getLevenshteinDistance("hippo", "elephant") = 7
	 * StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz") = 8
	 * StringUtils.getLevenshteinDistance("hello", "hallo")    = 1
	 * </pre>
	 *
	 * @param s
	 *            the first String, must not be null
	 * @param t
	 *            the second String, must not be null
	 * @return result distance
	 * @throws IllegalArgumentException
	 *             if either String input {@code null}
	 * @since 3.0 Changed signature from getLevenshteinDistance(String, String)
	 *        to getLevenshteinDistance(CharSequence, CharSequence)
	 */
	public static int levenshteinDistance(CharSequence s, CharSequence t, int treshold) {
		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		// int dist = 0;
		//
		// if (Math.abs(dist = s.length() - t.length()) > treshold)
		// return 1000;
		//
		// final boolean matchesLengthCondition = Math.pow(1 - Math.pow(dist /
		// Math.max(s.length(), t.length()), 2),
		// 2) >= 0.9;
		//
		// if (matchesLengthCondition)
		// return 3000;

		/*
		 * The difference between this impl. and the previous is that, rather
		 * than creating and retaining a matrix of size s.length() + 1 by
		 * t.length() + 1, we maintain two single-dimensional arrays of length
		 * s.length() + 1. The first, d, is the 'current working' distance array
		 * that maintains the newest distance cost counts as we iterate through
		 * the characters of String s. Each time we increment the index of
		 * String t we are comparing, d is copied to p, the second int[]. Doing
		 * so allows us to retain the previous cost counts as required by the
		 * algorithm (taking the minimum of the cost count to the left, up one,
		 * and diagonally up and to the left of the current cost count being
		 * calculated). (Note that the arrays aren't really copied anymore, just
		 * switched...this is clearly much better than cloning an array or doing
		 * a System.arraycopy() each time through the outer loop.)
		 * 
		 * Effectively, the difference between the two implementations is this
		 * one does not cause an out of memory condition when calculating the LD
		 * over two very large strings.
		 */
		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		if (n > m) {
			// swap the input strings to consume less memory
			final CharSequence tmp = s;
			s = t;
			t = tmp;
			n = m;
			m = t.length();
		}

		int p[] = new int[n + 1]; // 'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			int min = Integer.MAX_VALUE;
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
				min = Math.min(min, d[i]);
			}
			if (min > treshold)
				return Math.max(s.length(), t.length());

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}

	public static int levenshtein(String s, String t, int threshold) {
		System.out.println("s = " + s);
		System.out.println("t = " + t);
		int slen = s.length();
		int tlen = t.length();

		// swap so the smaller string is t; this reduces the memory usage
		// of our buffers
		if (tlen > slen) {
			String stmp = s;
			s = t;
			t = stmp;
			int itmp = slen;
			slen = tlen;
			tlen = itmp;
		}

		// p is the previous and d is the current distance array; dtmp is used
		// in swaps
		int[] p = new int[tlen + 1];
		int[] d = new int[tlen + 1];
		int[] dtmp;

		// the values necessary for our threshold are written; the ones after
		// must be filled with large integers since the tailing member of the
		// threshold
		// window in the bottom array will run min across them
		int n = 0;
		for (; n < Math.min(p.length, threshold + 1); ++n)
			p[n] = n;
		Arrays.fill(p, n, p.length, Integer.MAX_VALUE);
		Arrays.fill(d, Integer.MAX_VALUE);

		// this is the core of the Levenshtein edit distance algorithm
		// instead of actually building the matrix, two arrays are swapped back
		// and forth
		// the threshold limits the amount of entries that need to be computed
		// if we're
		// looking for a match within a set distance
		for (int row = 1; row < s.length() + 1; ++row) {
			char schar = s.charAt(row - 1);
			d[0] = row;

			// set up our threshold window
			int min = Math.max(1, row - threshold);
			int max = Math.min(d.length, row + threshold + 1);

			// since we're reusing arrays, we need to be sure to wipe the value
			// left of the
			// starting index; we don't have to worry about the value above the
			// ending index
			// as the arrays were initially filled with large integers and we
			// progress to the right
			if (min > 1)
				d[min - 1] = Integer.MAX_VALUE;

			for (int col = min; col < max; ++col) {
				if (schar == t.charAt(col - 1))
					d[col] = p[col - 1];
				else
					// min of: diagonal, left, up
					d[col] = Math.min(p[col - 1], Math.min(d[col - 1], p[col])) + 1;
			}
			// swap our arrays
			dtmp = p;
			p = d;
			d = dtmp;
		}

		if (p[tlen] == Integer.MAX_VALUE)
			return -1;
		return p[tlen];
	}

}

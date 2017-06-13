package candidateretrieval;

import java.util.Arrays;
import java.util.List;

public class Stopwords {

	public static final List<String> DISEASE_ENGLISH_STOP_WORDS = Arrays.asList("of", "the", "a", "an", "in");

	public static final List<String> ENGLISH_STOP_WORDS = Arrays.asList("a", "an", "and", "are", "as", "at", "be",
			"but", "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the",
			"their", "then", "there", "these", "they", "this", "to", "was", "will", "with", "very", "from", "all");

	final public static List<String> BODY_PART_STOP_WORDS = Arrays.asList("other", "to", "at", "the", "process",
			"extends", "through");

	final public static List<String> NUMBERS_STOP_WORDS = Arrays.asList("i", "ii", "iii", "1", "2", "3", "4", "5", "6",
			"7", "8", "9", "0", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "one", "two", "three",
			"four", "five", "six", "seven", "eight", "nine", "zero", "first", "second", "third");

}

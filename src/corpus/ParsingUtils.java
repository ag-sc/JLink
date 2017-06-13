package corpus;

import java.util.List;

import variables.Token;

public class ParsingUtils {

	public static int binarySpanSearch(List<Token> tokens, int characterPosition, boolean findLowerBound) {
		int low = 0;
		int high = tokens.size();
		int middle = 0;
		while (low < high) {
			middle = (high + low) / 2;
			Token midToken = tokens.get(middle);
			if (findLowerBound ? characterPosition < midToken.getFrom() : characterPosition <= midToken.getFrom())
				high = middle;
			else if (findLowerBound ? midToken.getTo() <= characterPosition : midToken.getTo() < characterPosition)
				low = middle + 1;
			else
				return middle;
		}
		middle = (high + low) / 2;
		// Log.w("No token for position %s found. Last boundaries %s-%s-%s.
		// Return middle: %s.", characterPosition, low,
		// middle, high, middle);
		return middle;
	}

	// public static int binarySearch(int characterPosition, List<Token> tokens)
	// {
	// return binarySearch(characterPosition, tokens, false);
	// }
}

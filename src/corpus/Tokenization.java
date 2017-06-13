package corpus;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import variables.Token;

public class Tokenization {

	private static Logger log = LogManager.getFormatterLogger(Tokenization.class.getName());

	public List<Token> tokens;
	public String originalSentence;
	public int absoluteStartOffset;
	public int absoluteEndOffset;

	public Tokenization(List<Token> tokens, String originalSentence, int absoluteStartOffset) {
		this.tokens = tokens;
		this.originalSentence = originalSentence;
		this.absoluteStartOffset = absoluteStartOffset;
		this.absoluteEndOffset = absoluteStartOffset + originalSentence.length();
	}

	@Override
	public String toString() {
		return "Tokenization [" + absoluteStartOffset + "-" + absoluteEndOffset + ": " + originalSentence + "\n\t"
				+ tokens + "]";
	}

}

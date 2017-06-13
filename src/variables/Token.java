package variables;

import java.io.Serializable;

public class Token implements Serializable {

	/**
	 * Position of this token in the list of tokens that make up the tokenized
	 * document.
	 */
	private int index;
	/**
	 * Character position of this token in the original text.
	 */
	private int from, to;
	/**
	 * Piece of text of the original document that the token offsets (from, to)
	 * correspond to.
	 */
	private String text;

	public Token(int index, int start, int stop, String text) {
		super();
		this.index = index;
		this.from = start;
		this.to = stop;
		this.text = text;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public String getText() {
		return text;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "Token [" + index + ": " + text + " (" + from + "-" + to + ")]";
	}
}

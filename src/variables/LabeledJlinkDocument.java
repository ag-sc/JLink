package variables;

import java.util.Collections;
import java.util.List;

import corpus.LabeledInstance;

/**
 * This class implements the LabeledInstance interface for documents by
 * extending from the Document class and extending it by the expected gold
 * result.
 * 
 * @author sjebbara
 */
public class LabeledJlinkDocument implements LabeledInstance<LabeledJlinkDocument, JLinkState> {
	/**
	 * This object holds the (human) labeled, correct result, that should be
	 * used during training and evaluation.
	 */
	protected JLinkState goldResult;
	protected String name;
	protected String content;
	protected List<Token> tokens;

	public LabeledJlinkDocument(String name, String content, List<Token> tokens) {
		this.name = name;
		this.content = content;
		this.tokens = tokens;
	}

	public String getContent() {
		return content;
	}

	public List<Token> getTokens() {
		return Collections.unmodifiableList(tokens);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "LabeledDocument [content=" + content + ", tokens=" + tokens + ", goldState=" + goldResult + "]";
	}

	public void setGoldResult(JLinkState goldQuery) {
		this.goldResult = goldQuery;
	}

	@Override
	public LabeledJlinkDocument getInstance() {
		return this;
	}

	@Override
	public JLinkState getResult() {
		return goldResult;
	}

}

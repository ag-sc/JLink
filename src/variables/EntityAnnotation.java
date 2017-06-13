package variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dictionary.Concept;
import tokenization.Tokenizer;

public class EntityAnnotation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * This number specifies the token index (!! not character offset) of the
	 * first token that this annotation references.
	 */
	protected int beginTokenIndex;
	/**
	 * This number specifies the token index (!! not character offset) of the
	 * last token that this annotation references.
	 */
	protected int endTokenIndex;

	protected Concept concept;

	protected String originalText;
	protected int originalStart;
	protected int originalEnd;

	protected boolean isSyntacticTransformation;
	protected int synonymReplacementIndex;
	protected String synonymReplacementToken;

	protected boolean isMorphologicalTransformation;
	protected String morphologicalReplacement;

	protected String forwardRule;

	public boolean isMorphologicalTransformation() {
		return isMorphologicalTransformation;
	}

	public String getMorphologicalReplacement() {
		return morphologicalReplacement;
	}

	protected String entityID;

	protected String realToken;

	protected double synonymReplacementFactor;

	protected final JLinkState state;

	public String getForwardRule() {
		return forwardRule;
	}

	/**
	 * This flag can be used to declare an annotation as fixed, so that the
	 * Sampler/Explorers don't change this annotation in the processing of
	 * sample generation.
	 */
	protected boolean isPriorKnowledge = false;

	final protected String text;

	/**
	 * Clone constructor.
	 * 
	 * @param state
	 * @param e
	 */
	public EntityAnnotation(JLinkState state, EntityAnnotation e) {
		this.state = state;
		this.concept = e.concept;
		this.beginTokenIndex = e.beginTokenIndex;
		this.endTokenIndex = e.endTokenIndex;
		this.originalText = e.originalText;
		this.originalStart = e.originalStart;
		this.originalEnd = e.originalEnd;
		this.isPriorKnowledge = e.isPriorKnowledge;
		this.entityID = e.entityID;
		this.synonymReplacementIndex = e.synonymReplacementIndex;
		this.synonymReplacementToken = e.synonymReplacementToken;
		this.synonymReplacementFactor = e.synonymReplacementFactor;
		this.realToken = e.realToken;
		this.isSyntacticTransformation = e.isSyntacticTransformation;
		this.isMorphologicalTransformation = e.isMorphologicalTransformation;
		this.morphologicalReplacement = e.morphologicalReplacement;
		this.forwardRule = e.forwardRule;
		this.text = buildText();
	}

	public EntityAnnotation(JLinkState state, Concept Concept, String originalText, int tokenStart, int tokenEnd,
			int charStart, int charEnd) {
		this.originalText = originalText;
		this.state = state;
		this.concept = Concept;
		this.beginTokenIndex = tokenStart;
		this.endTokenIndex = tokenEnd;
		this.entityID = UUID.randomUUID().toString();
		this.originalStart = charStart;
		this.originalEnd = charEnd;
		this.text = buildText();
	}

	public EntityAnnotation(JLinkState state, Concept Concept, int tokenStart, int tokenEnd,
			String morphologicalReplacement, final String forwardRule) {
		this.state = state;
		this.concept = Concept;
		this.beginTokenIndex = tokenStart;
		this.endTokenIndex = tokenEnd;
		this.entityID = UUID.randomUUID().toString();
		this.isMorphologicalTransformation = true;
		this.morphologicalReplacement = morphologicalReplacement;
		this.forwardRule = forwardRule;
		this.text = buildText();
	}

	public EntityAnnotation(JLinkState state, Concept Concept, int start, int end, int synonymReplacementIndex,
			String synonymReplacementToken, double synonymScore, String realToken) {
		this.state = state;
		this.concept = Concept;
		this.beginTokenIndex = start;
		this.endTokenIndex = end;
		this.entityID = UUID.randomUUID().toString();
		this.synonymReplacementIndex = synonymReplacementIndex;
		this.synonymReplacementToken = synonymReplacementToken;
		this.synonymReplacementFactor = synonymScore;
		this.isSyntacticTransformation = true;
		this.realToken = realToken;
		this.text = buildText();
	}

	public boolean isSyntacticTransformation() {
		return isSyntacticTransformation;
	}

	public void setSyntacticTransformation(boolean isSyntacticTransformation) {
		this.isSyntacticTransformation = isSyntacticTransformation;
	}

	public JLinkState getState() {
		return state;
	}

	public Concept getType() {
		return concept;
	}

	public int getBeginTokenIndex() {
		return beginTokenIndex;
	}

	public int getEndTokenIndex() {
		return endTokenIndex;
	}

	public String getText() {
		return text;
	}

	private String buildText() {

		if (isMorphologicalTransformation) {
			return Tokenizer.getTokenizedForm(morphologicalReplacement);
		}

		List<Token> tokens = getTokens();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (isSyntacticTransformation && i == synonymReplacementIndex && synonymReplacementToken != null) {
				builder.append(synonymReplacementToken);
			} else {
				builder.append(token.getText());
			}

			/*
			 * Add a whitespace if the following token does not connect directly
			 * to this one (e.g not "interleukin" and "-")
			 */
			if (i < tokens.size() - 1 && tokens.get(i).getTo() < tokens.get(i + 1).getFrom()) {
				builder.append(" ");
			}
		}
		return Tokenizer.getTokenizedForm(builder.toString());

	}

	/**
	 * Returns the entity that is associated with the specified ID, using this
	 * entities's parent state.
	 * 
	 * @param id
	 * @return
	 */
	public EntityAnnotation getEntity(String id) {
		return state.getEntity(id);
	}

	public String getEntityID() {
		return entityID;
	}

	public List<Token> getTokens() {
		List<Token> tokens = new ArrayList<Token>();
		for (int i = beginTokenIndex; i < endTokenIndex; i++)
			tokens.add(state.getDocument().getTokens().get(i));
		return tokens;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public int getOriginalStart() {
		return originalStart;
	}

	public void setOriginalStart(int originalStart) {
		this.originalStart = originalStart;
	}

	public int getOriginalEnd() {
		return originalEnd;
	}

	public void setOriginalEnd(int originalEnd) {
		this.originalEnd = originalEnd;
	}

	public String getRealToken() {
		return realToken;
	}

	public void setRealToken(String realToken) {
		this.realToken = realToken;
	}

	public int getSynonymReplacementIndex() {
		return synonymReplacementIndex;
	}

	public void setSynonymReplacementIndex(int synonymReplacementIndex) {
		this.synonymReplacementIndex = synonymReplacementIndex;
	}

	public String getSynonymReplacementToken() {
		return synonymReplacementToken;
	}

	public void setSynonymReplacementToken(String synonymReplacementToken) {
		this.synonymReplacementToken = synonymReplacementToken;
	}

	public void setType(Concept type) {
		this.concept = type;
	}
	// //
	// // /**
	// // * Marks this annotation as fixed. That means, that the sampler should
	// not
	// // * changes this annotation during the sampling process. This is used to
	// // * initialize a state with fixed prior knowledge.
	// // *
	// // * @param b
	// // */
	// // public void setPriorKnowledge(boolean b) {
	// // this.isPriorKnowledge = b;
	// // }
	// //
	// // public boolean isPriorKnowledge() {
	// // return isPriorKnowledge;
	// // }
	//
	// public void setBeginTokenIndex(int beginTokenIndex) {
	// // TODO this handling of changes is not perfectly efficient and allows
	// // errors and inconsistencies if applied wrongly
	// state.removeFromTokenToEntityMapping(this);
	// this.beginTokenIndex = beginTokenIndex;
	// state.addToTokenToEntityMapping(this);
	// }
	//
	// public void setEndTokenIndex(int endTokenIndex) {
	// // TODO this handling of changes is not perfectly efficient and allows
	// // errors and inconsistencies if applied wrongly
	//
	// state.removeFromTokenToEntityMapping(this);
	// this.endTokenIndex = endTokenIndex;
	// state.addToTokenToEntityMapping(this);
	// }

	// public String getText() {
	// List<Token> tokens = getTokens();
	// StringBuilder builder = new StringBuilder();
	// for (int i = 0; i < tokens.size(); i++) {
	// Token token = tokens.get(i);
	// builder.append(token.getText());
	//
	// /*
	// * Add a whitespace if the following token does not connect directly
	// * to this one (e.g not "interleukin" and "-")
	// */
	// if (i < tokens.size() - 1 && tokens.get(i).getTo() < tokens.get(i +
	// 1).getFrom()) {
	// builder.append(" ");
	// }
	// }
	// return builder.toString();
	// }

	@Override
	public String toString() {
		return "EntityAnnotation [beginTokenIndex=" + beginTokenIndex + ", endTokenIndex=" + endTokenIndex + ", type="
				+ concept + ", isSyntacticTransformation=" + isSyntacticTransformation + ", synonymReplacementToken="
				+ synonymReplacementToken + ", isMorphologicalTransformation=" + isMorphologicalTransformation
				+ ", morphologicalReplacement=" + morphologicalReplacement + ", forwardRule=" + forwardRule
				+ ", realToken=" + realToken + ", synonymReplacementFactor=" + synonymReplacementFactor + ", text="
				+ text + "]";
	}

	public double getSynonymReplacementFactor() {
		return synonymReplacementFactor;
	}

	public void setSynonymReplacementFactor(double synonymReplacementFactor) {
		this.synonymReplacementFactor = synonymReplacementFactor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + beginTokenIndex;
		result = prime * result + endTokenIndex;
		result = prime * result + (isMorphologicalTransformation ? 1231 : 1237);
		result = prime * result + (isSyntacticTransformation ? 1231 : 1237);
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((concept == null) ? 0 : concept.hashCode());
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
		EntityAnnotation other = (EntityAnnotation) obj;
		if (beginTokenIndex != other.beginTokenIndex)
			return false;
		if (endTokenIndex != other.endTokenIndex)
			return false;
		if (isMorphologicalTransformation != other.isMorphologicalTransformation)
			return false;
		if (isSyntacticTransformation != other.isSyntacticTransformation)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (concept == null) {
			if (other.concept != null)
				return false;
		} else if (!concept.equals(other.concept))
			return false;
		return true;
	}

}

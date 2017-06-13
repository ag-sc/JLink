package main.comparisonobjects;

import dictionary.Concept;
import variables.EntityAnnotation;

/**
 * Comnpares the offset, the actual String representation AND the assigned ID.
 * 
 * @author hterhors
 *
 */
public class EntityRecAndDisambComparisonObject {

	final public int fromToken;

	final public int toToken;

	final public String mention;

	final public Concept id;

	public EntityRecAndDisambComparisonObject(int fromToken, int toToken, String mention, Concept id) {
		super();
		this.fromToken = fromToken;
		this.toToken = toToken;
		this.mention = mention;
		this.id = id;
	}

	public EntityRecAndDisambComparisonObject(Annotation entity) {
		this.fromToken = entity.begin;
		this.toToken = entity.end;
		this.mention = "";
		this.id = entity.concept;
	}

	public EntityRecAndDisambComparisonObject(EntityAnnotation entity) {
		this.fromToken = entity.getBeginTokenIndex();
		this.toToken = entity.getEndTokenIndex();
		this.mention = entity.getText();
		this.id = entity.getType();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.fromToken;
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + ((this.mention == null) ? 0 : this.mention.hashCode());
		result = prime * result + this.toToken;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EntityRecAndDisambComparisonObject))
			return false;
		EntityRecAndDisambComparisonObject other = (EntityRecAndDisambComparisonObject) obj;
		if (this.fromToken != other.fromToken)
			return false;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		if (this.mention == null) {
			if (other.mention != null)
				return false;
		} else if (!this.mention.equals(other.mention))
			return false;
		if (this.toToken != other.toToken)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityComparisonObject [fromToken=" + this.fromToken + ", toToken=" + this.toToken + ", mention="
				+ this.mention + ", id=" + this.id + "]";
	}

}

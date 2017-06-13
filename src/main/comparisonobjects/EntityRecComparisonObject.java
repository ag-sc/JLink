package main.comparisonobjects;

import variables.EntityAnnotation;

/**
 * Compares only the offset and the actual String representation. The assigned
 * ID is not important.
 * 
 * @author hterhors
 *
 */
public class EntityRecComparisonObject {

	final public int fromToken;

	final public int toToken;

	final public String mention;

	public EntityRecComparisonObject(Annotation goldEntity) {
		this.fromToken = goldEntity.begin;
		this.toToken = goldEntity.end;
		this.mention = "";
	}

	public EntityRecComparisonObject(EntityAnnotation entity) {
		this.fromToken = entity.getOriginalStart();
		this.toToken = entity.getOriginalEnd();
		this.mention = entity.getOriginalText();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fromToken;
		result = prime * result + ((mention == null) ? 0 : mention.hashCode());
		result = prime * result + toToken;
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
		EntityRecComparisonObject other = (EntityRecComparisonObject) obj;
		if (fromToken != other.fromToken)
			return false;
		if (mention == null) {
			if (other.mention != null)
				return false;
		} else if (!mention.equals(other.mention))
			return false;
		if (toToken != other.toToken)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityRecComparisonObject [fromToken=" + fromToken + ", toToken=" + toToken + ", mention=" + mention
				+ "]";
	}

}

package main.comparisonobjects;

import dictionary.Concept;
import variables.EntityAnnotation;

/**
 * Compares only the disambiguation as a set of IDs.
 * 
 * @author hterhors
 *
 */
public class EntityDisambComparisonObject {

	final public Concept id;

	public EntityDisambComparisonObject(EntityAnnotation entity) {
		this.id = entity.getType();
	}

	public EntityDisambComparisonObject(Annotation goldEntity) {
		id = goldEntity.concept;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		EntityDisambComparisonObject other = (EntityDisambComparisonObject) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id.conceptID;
	}

}

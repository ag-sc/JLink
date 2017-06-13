package dictionary;

import java.util.HashSet;
import java.util.Set;

import variables.EEntityType;

public class Concept {

	final public String conceptID;

	final public EEntityType entityType;

	final public EOrigin origin;

	final public Set<String> alternateConceptIDs;

	public Concept(Concept clone) {
		this.conceptID = clone.conceptID;
		this.entityType = clone.entityType;
		this.origin = clone.origin;
		this.alternateConceptIDs = clone.alternateConceptIDs;
	}

	public Concept(String conceptID, EOrigin origin, Set<String> alternateConceptIDs, EEntityType entityType) {
		this.conceptID = conceptID;
		this.origin = origin;
		this.alternateConceptIDs = alternateConceptIDs;
		this.entityType = entityType;
	}

	public Concept(String conceptID, EOrigin origin, EEntityType entityType) {
		this.conceptID = conceptID;
		this.origin = origin;
		this.entityType = entityType;
		this.alternateConceptIDs = new HashSet<>();
	}

	public Concept(String conceptID, EEntityType entityType) {
		this.conceptID = conceptID;
		this.entityType = entityType;
		this.origin = EOrigin.UNK;
		this.alternateConceptIDs = new HashSet<>();
	}

	public String getConceptID() {
		return conceptID;
	}

	public EOrigin getOrigin() {
		return origin;
	}

	public Set<String> getAlternateConceptIDs() {
		return alternateConceptIDs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conceptID == null) ? 0 : conceptID.hashCode());
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
		Concept other = (Concept) obj;
		if (conceptID == null) {
			if (other.conceptID != null)
				return false;
		} else if (!conceptID.equals(other.conceptID))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Concept [conceptID=" + conceptID + ", origin=" + origin + ", alternateConceptIDs=" + alternateConceptIDs
				+ "]";
	}

}

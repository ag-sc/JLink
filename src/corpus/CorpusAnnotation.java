package corpus;

import variables.EEntityType;

public class CorpusAnnotation {
	final public String conceptID;
	final public int start;
	final public int end;
	final public String surfaceForm;
	final public String documentID;
	final public EEntityType entityType;

	public CorpusAnnotation(String conceptID, int start, int end, String surfaceForm, String documentID,
			EEntityType entityType) {
		this.conceptID = conceptID;
		this.start = start;
		this.end = end;
		this.surfaceForm = surfaceForm;
		this.documentID = documentID;
		this.entityType = entityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conceptID == null) ? 0 : conceptID.hashCode());
		result = prime * result + ((documentID == null) ? 0 : documentID.hashCode());
		result = prime * result + end;
		result = prime * result + start;
		result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
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
		CorpusAnnotation other = (CorpusAnnotation) obj;
		if (conceptID == null) {
			if (other.conceptID != null)
				return false;
		} else if (!conceptID.equals(other.conceptID))
			return false;
		if (documentID == null) {
			if (other.documentID != null)
				return false;
		} else if (!documentID.equals(other.documentID))
			return false;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		if (surfaceForm == null) {
			if (other.surfaceForm != null)
				return false;
		} else if (!surfaceForm.equals(other.surfaceForm))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CorpusAnnotation [conceptID=" + conceptID + ", start=" + start + ", end=" + end + ", surfaceForm="
				+ surfaceForm + ", documentID=" + documentID + "]";
	}

}

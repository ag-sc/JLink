package main.comparisonobjects;

import dictionary.Concept;

class Annotation {

	public final Concept concept;
	public final int begin;
	public final int end;

	public Annotation(Concept concept, int begin, int end) {
		this.concept = concept;
		this.begin = begin;
		this.end = end;
	}

	@Override
	public String toString() {
		return "Annotation [diseaseID=" + concept + ", begin=" + begin + ", end=" + end + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + begin;
		result = prime * result + ((concept == null) ? 0 : concept.hashCode());
		result = prime * result + end;
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
		Annotation other = (Annotation) obj;
		if (begin != other.begin)
			return false;
		if (concept == null) {
			if (other.concept != null)
				return false;
		} else if (!concept.equals(other.concept))
			return false;
		if (end != other.end)
			return false;
		return true;
	}

}

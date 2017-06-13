package candidateretrieval.lucene;

import dictionary.Concept;

public class LuceneCandidate implements Comparable<LuceneCandidate> {

	final public Concept concept;
	final public double confidence;

	public LuceneCandidate(Concept result, float value) {
		this.concept = result;
		this.confidence = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((concept == null) ? 0 : concept.hashCode());
		long temp;
		temp = Double.doubleToLongBits(confidence);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		LuceneCandidate other = (LuceneCandidate) obj;
		if (concept == null) {
			if (other.concept != null)
				return false;
		} else if (!concept.equals(other.concept))
			return false;
		if (Double.doubleToLongBits(confidence) != Double.doubleToLongBits(other.confidence))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LuceneResult [result=" + concept + ", value=" + confidence + "]";
	}

	@Override
	public int compareTo(LuceneCandidate o) {
		return -Double.compare(this.confidence, o.confidence);
	}

}

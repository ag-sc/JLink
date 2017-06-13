package candidateretrieval.jaccard;

import dictionary.Concept;

public class JaccardCandidate implements Comparable<JaccardCandidate> {

	public final Concept concept;
	public final double confidence;

	public JaccardCandidate(Concept concept, double value) {
		this.concept = concept;
		this.confidence = value;
	}

	@Override
	public String toString() {
		return "JeccardEntity [concept=" + concept + ", value=" + confidence + "]";
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
		JaccardCandidate other = (JaccardCandidate) obj;
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
	public int compareTo(JaccardCandidate o) {
		return -Double.compare(this.confidence, o.confidence);
	}

}

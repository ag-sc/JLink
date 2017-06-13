package templates.container;

import java.util.Set;

public class FuzzyAnnotationContainer {

	public final String bestMatch;
	public final String diseaseID;
	public final Set<CharacterRule> rules;
	public final double score;
	public final boolean isPrefForm;

	public FuzzyAnnotationContainer(String bestMatch, String diseaseID, Set<CharacterRule> rules, double score,
			boolean isPrefForm) {
		super();
		this.bestMatch = bestMatch;
		this.diseaseID = diseaseID;
		this.rules = rules;
		this.score = score;
		this.isPrefForm = isPrefForm;
	}

	@Override
	public String toString() {
		return "FuzzyAnnotationContainer [bestMatch=" + bestMatch + ", diseaseID=" + diseaseID + ", rules=" + rules
				+ ", score=" + score + ", isPrefForm=" + isPrefForm + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bestMatch == null) ? 0 : bestMatch.hashCode());
		result = prime * result + ((diseaseID == null) ? 0 : diseaseID.hashCode());
		result = prime * result + (isPrefForm ? 1231 : 1237);
		result = prime * result + ((rules == null) ? 0 : rules.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
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
		FuzzyAnnotationContainer other = (FuzzyAnnotationContainer) obj;
		if (bestMatch == null) {
			if (other.bestMatch != null)
				return false;
		} else if (!bestMatch.equals(other.bestMatch))
			return false;
		if (diseaseID == null) {
			if (other.diseaseID != null)
				return false;
		} else if (!diseaseID.equals(other.diseaseID))
			return false;
		if (isPrefForm != other.isPrefForm)
			return false;
		if (rules == null) {
			if (other.rules != null)
				return false;
		} else if (!rules.equals(other.rules))
			return false;
		if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
			return false;
		return true;
	}

}

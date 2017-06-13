package templates.container;

public class CharacterRule {
	public final String fromString;
	public final String toString;
	public final int errorIndexForward;
	public final int errorIndexBackward;
	public final String forwardRule;
	public final String backwardRule;

	public CharacterRule(final String fromString, String toString, int errorIndexForward, int errorIndexBackward,
			String forwardRule, String backwardRule) {
		this.fromString = fromString;
		this.toString = toString;
		this.errorIndexForward = errorIndexForward;
		this.errorIndexBackward = errorIndexBackward;
		this.forwardRule = forwardRule + "->" + backwardRule;
		this.backwardRule = backwardRule + "->" + forwardRule;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((backwardRule == null) ? 0 : backwardRule.hashCode());
		result = prime * result + ((toString == null) ? 0 : toString.hashCode());
		result = prime * result + errorIndexBackward;
		result = prime * result + errorIndexForward;
		result = prime * result + ((forwardRule == null) ? 0 : forwardRule.hashCode());
		result = prime * result + ((fromString == null) ? 0 : fromString.hashCode());
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
		CharacterRule other = (CharacterRule) obj;
		if (backwardRule == null) {
			if (other.backwardRule != null)
				return false;
		} else if (!backwardRule.equals(other.backwardRule))
			return false;
		if (toString == null) {
			if (other.toString != null)
				return false;
		} else if (!toString.equals(other.toString))
			return false;
		if (errorIndexBackward != other.errorIndexBackward)
			return false;
		if (errorIndexForward != other.errorIndexForward)
			return false;
		if (forwardRule == null) {
			if (other.forwardRule != null)
				return false;
		} else if (!forwardRule.equals(other.forwardRule))
			return false;
		if (fromString == null) {
			if (other.fromString != null)
				return false;
		} else if (!fromString.equals(other.fromString))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CharacterRule [fromString=" + fromString + ", toString=" + toString + ", errorIndexForward="
				+ errorIndexForward + ", errorIndexBackward=" + errorIndexBackward + ", forwardRule=" + forwardRule
				+ ", backwardRule=" + backwardRule + "]";
	}

}

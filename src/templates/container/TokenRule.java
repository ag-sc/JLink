package templates.container;

public class TokenRule {

	public final String forwardRule;
	public final String backwardRule;

	public TokenRule(String a, String b) {
		this.forwardRule = a + "->" + b;
		this.backwardRule = b + "->" + a;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((backwardRule == null) ? 0 : backwardRule.hashCode());
		result = prime * result + ((forwardRule == null) ? 0 : forwardRule.hashCode());
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
		TokenRule other = (TokenRule) obj;
		if (backwardRule == null) {
			if (other.backwardRule != null)
				return false;
		} else if (!backwardRule.equals(other.backwardRule))
			return false;
		if (forwardRule == null) {
			if (other.forwardRule != null)
				return false;
		} else if (!forwardRule.equals(other.forwardRule))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CharachterRule [forwardRule=" + forwardRule + ", backwardRule=" + backwardRule + "]";
	}

}

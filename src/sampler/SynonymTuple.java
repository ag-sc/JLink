package sampler;

public class SynonymTuple implements Comparable<SynonymTuple> {

	final public String token;
	final public double score;

	public SynonymTuple(String token, double score) {
		this.token = token;
		this.score = score;
	}

	@Override
	public String toString() {
		return "SynonymTuple [token=" + token + ", score=" + score + "]";
	}

	@Override
	public int compareTo(SynonymTuple st) {
		return -Double.compare(score, st.score);
	}

}

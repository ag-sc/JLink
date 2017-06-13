package stoppingcriterion;

import java.util.List;

import sampling.stoppingcriterion.StoppingCriterion;
import variables.JLinkState;

public class StopAtMaxObjectiveScore implements StoppingCriterion<JLinkState> {
	final int numberOfSamplingStepsTRAIN;

	public StopAtMaxObjectiveScore(int numberOfSamplingStepsTEST) {
		this.numberOfSamplingStepsTRAIN = numberOfSamplingStepsTEST;
	}

	@Override
	public boolean checkCondition(List<JLinkState> chain, int step) {
		if (chain.isEmpty())
			return false;

		double maxScore = chain.get(chain.size() - 1).getObjectiveScore();
		int count = 0;
		final int maxCount = 3;

		for (int i = 0; i < chain.size(); i++) {
			if (chain.get(i).getObjectiveScore() >= maxScore) {
				count++;
			}
		}

		if (step >= numberOfSamplingStepsTRAIN)
			System.err.println("NUMBER OF SAMPLING STEPS EXCEEDED!");

		return count >= maxCount || chain.get(chain.size() - 1).getObjectiveScore() >= 1
				|| step >= numberOfSamplingStepsTRAIN;
	}

}

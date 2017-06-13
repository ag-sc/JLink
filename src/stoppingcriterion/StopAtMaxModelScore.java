package stoppingcriterion;

import java.util.List;

import sampling.stoppingcriterion.StoppingCriterion;
import variables.JLinkState;

public class StopAtMaxModelScore implements StoppingCriterion<JLinkState> {
	final int numberOfSamplingStepsTEST;

	public StopAtMaxModelScore(int numberOfSamplingStepsTEST) {
		this.numberOfSamplingStepsTEST = numberOfSamplingStepsTEST;
	}

	@Override
	public boolean checkCondition(List<JLinkState> chain, int step) {

		if (chain.isEmpty())
			return false;

		double maxScore = chain.get(chain.size() - 1).getModelScore();
		int count = 0;
		final int maxCount = 3;

		for (int i = 0; i < chain.size(); i++) {
			if (chain.get(i).getModelScore() >= maxScore) {
				count++;
			}
		}
		if (step >= numberOfSamplingStepsTEST) {
			System.err.println("NUMBER OF SAMPLING STEPS EXCEEDED!");
		}

		return count >= maxCount || step >= numberOfSamplingStepsTEST;
	}

}

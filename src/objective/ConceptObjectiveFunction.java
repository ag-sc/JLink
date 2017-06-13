package objective;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import learning.ObjectiveFunction;
import main.comparisonobjects.EntityDisambComparisonObject;
import main.evaluation.PRF1Extended;
import variables.JLinkState;
import variables.EntityAnnotation;

public class ConceptObjectiveFunction extends ObjectiveFunction<JLinkState, JLinkState> implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(ConceptObjectiveFunction.class.getName());

	public ConceptObjectiveFunction() {
	}

	@Override
	public double computeScore(JLinkState resultState, JLinkState goldState) {
		Map<String, Set<String>> gold = new HashMap<String, Set<String>>();
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();

		/*
		 * Split to get on documents name level instead of the sentence level.
		 */
		final String resultKey = resultState.getDocument().getName().split("-")[0];
		result.putIfAbsent(resultKey, new HashSet<String>());
		for (EntityAnnotation resultEntity : resultState.getEntities()) {
			result.get(resultKey).add(new EntityDisambComparisonObject(resultEntity).toString());
		}

		/*
		 * Split to get on documents name level instead of the sentence level.
		 */
		final String goldKey = goldState.getDocument().getName().split("-")[0];
		gold.putIfAbsent(goldKey, new HashSet<String>());
		for (EntityAnnotation goldEntity : goldState.getEntities()) {
			gold.get(goldKey).add(new EntityDisambComparisonObject(goldEntity).toString());
		}

		return PRF1Extended.objectiveFunction(gold, result);

	}

}

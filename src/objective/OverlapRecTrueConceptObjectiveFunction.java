package objective;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import learning.ObjectiveFunction;
import variables.JLinkState;
import variables.EntityAnnotation;

public class OverlapRecTrueConceptObjectiveFunction extends ObjectiveFunction<JLinkState, JLinkState> implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(OverlapRecTrueConceptObjectiveFunction.class.getName());
	final private static double recFactor = 0.5;

	public OverlapRecTrueConceptObjectiveFunction() {
	}

	@Override
	public double computeScore(JLinkState state, JLinkState goldState) {

		Collection<EntityAnnotation> goldEntities = goldState.getEntities();
		Collection<EntityAnnotation> entities = state.getEntities();

		double f1Score;

		if (goldEntities.size() == 0 && entities.size() == 0) {
			f1Score = 1;
		} else {
			double precision = 0.0;
			List<EntityAnnotation> l = new ArrayList<>(goldEntities);

			Collections.sort(l, new Comparator<EntityAnnotation>() {

				@Override
				public int compare(EntityAnnotation o1, EntityAnnotation o2) {
					return Integer.compare(o1.getBeginTokenIndex(), o2.getBeginTokenIndex());
				}
			});
			List<EntityAnnotation> m = new ArrayList<>(entities);

			Collections.sort(m, new Comparator<EntityAnnotation>() {

				@Override
				public int compare(EntityAnnotation o1, EntityAnnotation o2) {
					return Integer.compare(o1.getBeginTokenIndex(), o2.getBeginTokenIndex());
				}
			});
			/*
			 * Prevent from using an annotation twice.
			 */
			Set<EntityAnnotation> noDoubleCheck = new HashSet<>();

			for (EntityAnnotation entity : m) {
				double max = 0.0;
				EntityAnnotation best = null;
				for (EntityAnnotation goldEntity : l) {

					if (noDoubleCheck.contains(goldEntity)) {
						continue;
					}

					final double finalScore = finalScore(entity, goldEntity);

					if (max < finalScore) {
						best = goldEntity;
						max = finalScore;
					}
				}
				if (best != null)
					noDoubleCheck.add(best);
				precision += max;
			}

			double recall = 0.0;

			noDoubleCheck = new HashSet<>();

			for (EntityAnnotation goldEntity : l) {
				double max = 0.0;
				EntityAnnotation best = null;

				for (EntityAnnotation entity : m) {
					final double finalScore = finalScore(goldEntity, entity);

					if (noDoubleCheck.contains(entity)) {
						continue;
					}
					if (max < finalScore) {
						best = entity;
						max = finalScore;
					}

				}
				if (best != null)
					noDoubleCheck.add(best);
				recall += max;
			}
			// System.out.println("Precision = " + precision + " / " +
			// entities.size());
			// System.out.println("Recall = " + recall + " / " +
			// goldEntities.size());

			if ((precision == 0 && recall == 0) || entities.size() == 0 || goldEntities.size() == 0) {
				f1Score = 0;
			} else {
				precision /= entities.size();
				recall /= goldEntities.size();

				f1Score = 2 * (precision * recall) / (precision + recall);
			}
		}
		// System.out.println("F1-Score = " + f1Score);
		return f1Score;
	}

	static private double finalScore(EntityAnnotation e1, EntityAnnotation e2) {
		double recognitionScore = overlapScore(e1, e2);
		double disambiguationScore = IDScore(e1, e2);
		final double finalScore = recFactor * recognitionScore + (1 - recFactor) * disambiguationScore;

		return finalScore;
	}

	static private double IDScore(EntityAnnotation entity, EntityAnnotation goldEntity) {
		return goldEntity.getType().equals(entity.getType()) ? 1 : 0;
	}

	public static double overlapScore(EntityAnnotation entity, EntityAnnotation goldEntity) {
		int a = entity.getBeginTokenIndex();
		int b = entity.getEndTokenIndex();
		int x = goldEntity.getBeginTokenIndex();
		int y = goldEntity.getEndTokenIndex();
		double overlap = (double) overlap(entity, goldEntity);
		double overlapScore = overlap / (b - a);
		return overlapScore;
	}

	public static int overlap(EntityAnnotation entity1, EntityAnnotation entity2) {
		int a = entity1.getBeginTokenIndex();
		int b = entity1.getEndTokenIndex();
		int x = entity2.getBeginTokenIndex();
		int y = entity2.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x));
		return overlap;
	}

}

package main.evaluation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.JLink;

public class PRF1Extended {

	public static double objectiveFunction(Map<String, Set<String>> goldData, Map<String, Set<String>> resultData) {

		for (String abstarctID : goldData.keySet()) {
			resultData.putIfAbsent(abstarctID, new HashSet<String>());
		}

		double macroF1 = 0;
		for (String pubmedID : goldData.keySet()) {
			macroF1 += PRF1Extended.macroF1(goldData.get(pubmedID), resultData.get(pubmedID)) / goldData.size();
			JLink.log.info("DocumentID		= " + pubmedID);
			JLink.log.info("Findings		= " + resultData.get(pubmedID));
			JLink.log.info("Gold			= " + goldData.get(pubmedID));
			JLink.log.info("Precision: " + macroPrecision(goldData.get(pubmedID), resultData.get(pubmedID)));
			JLink.log.info("Recall: " + macroRecall(goldData.get(pubmedID), resultData.get(pubmedID)));
			JLink.log.info("F1: " + macroF1(goldData.get(pubmedID), resultData.get(pubmedID)));
			List<String> x = new ArrayList<>(goldData.get(pubmedID));
			x.removeAll(resultData.get(pubmedID));
			List<String> y = new ArrayList<>(resultData.get(pubmedID));
			y.removeAll(goldData.get(pubmedID));
			JLink.log.info("Missing: " + x);
			JLink.log.info("To much: " + y);
			JLink.log.info("");
		}

		return macroF1;
	}

	public static void calculate(Map<String, Set<String>> goldData, Map<String, Set<String>> baselineData) {
		calculate(goldData, baselineData, false);
	}

	public static void calculate(Map<String, Set<String>> goldData, Map<String, Set<String>> baselineData,
			boolean print) {

		for (String abstarctID : goldData.keySet()) {
			baselineData.putIfAbsent(abstarctID, new HashSet<String>());
		}

		int tp = 0;
		int fp = 0;
		int fn = 0;
		double macroPrecision = 0;
		double macroRecall = 0;
		double macroF1 = 0;
		for (String pubmedID : goldData.keySet()) {

			tp += getTruePositives(goldData.get(pubmedID), baselineData.get(pubmedID));

			fp += getFalsePositives(goldData.get(pubmedID), baselineData.get(pubmedID));

			fn += getFalseNegatives(goldData.get(pubmedID), baselineData.get(pubmedID));

			macroPrecision += macroPrecision(goldData.get(pubmedID), baselineData.get(pubmedID));
			macroRecall += macroRecall(goldData.get(pubmedID), baselineData.get(pubmedID));
			// macroF1 += macroF1(goldData.get(pubmedID),
			// baselineData.get(pubmedID));
		}

		macroPrecision /= goldData.size();
		macroRecall /= goldData.size();
		// macroF1 /= goldData.size();
		// NCBI_DiseaseLearning.log.info("REAL = " + macroF1);
		macroF1 = macroF1(macroPrecision, macroRecall);
		// System.out.println("REAL = " + macroF1(macroPrecision, macroRecall));

		JLink.log.info("tp = " + round(tp));
		JLink.log.info("fp = " + round(fp));
		JLink.log.info("fn = " + round(fn));
		JLink.log.info("Micro precision = " + round(microPrecision(tp, fp, fn)));
		JLink.log.info("Micro recall = " + round(microRecall(tp, fp, fn)));
		JLink.log.info("Micro F1 = " + round(microF1(tp, fp, fn)));
		JLink.log.info("");
		JLink.log.info("Macro precision = " + round(macroPrecision));
		JLink.log.info("Macro recall = " + round(macroRecall));
		JLink.log.info("Macro F1 = " + round(macroF1));
		JLink.log.info("");

		if (print)
			JLink.results.print((round(microF1(tp, fp, fn))) + "\t" + round(microPrecision(tp, fp, fn)) + "\t"
					+ round(microRecall(tp, fp, fn)) + "\t" + round(macroF1) + "\t" + round(macroPrecision) + "\t"
					+ round(macroRecall) + "\t" + round(tp) + "\t" + round(fp) + "\t" + round(fn) + "\t");
	}

	private static double round(double d) {
		return Double.valueOf(new DecimalFormat("#.###").format(d));
	}

	public static double microPrecision(double tp, double fp, double fn) {
		return tp / (tp + fp);
	}

	public static double microRecall(double tp, double fp, double fn) {
		return tp / (tp + fn);
	}

	public static double microF1(double tp, double fp, double fn) {
		double p = microPrecision(tp, fp, fn);
		double r = microRecall(tp, fp, fn);
		double f1 = (2 * (p * r)) / (p + r);
		return f1;
	}

	public static int getTruePositives(Set<String> gold, Set<String> result) {
		Set<String> intersection = retainAllPrecision(gold, result);
		return intersection.size();
	}

	public static int getFalsePositives(Set<String> gold, Set<String> result) {
		/*
		 * Wrong!?!
		 */
		// Set<String> intersection = retainAllRecall(gold, result);
		Set<String> intersection = retainAllPrecision(gold, result);
		return result.size() - intersection.size();

	}

	public static int getFalseNegatives(Set<String> gold, Set<String> result) {
		Set<String> intersection = retainAllRecall(gold, result);
		return gold.size() - intersection.size();

	}

	public static double macroPrecision(Set<String> gold, Set<String> result) {

		if (result.size() == 0) {
			return 0;
		}

		Set<String> intersection = retainAllPrecision(gold, result);

		return (double) intersection.size() / result.size();

	}

	public static double macroRecall(Set<String> gold, Set<String> result) {

		if (gold.size() == 0) {
			return 0;
		}

		Set<String> intersection = retainAllRecall(gold, result);
		return (double) intersection.size() / gold.size();

	}

	public static double macroF1(double p, double r) {
		if (p == 0 || r == 0) {
			return 0;
		}
		double f1 = (2 * (p * r)) / (p + r);
		return f1;

	}

	public static double macroF1(Set<String> gold, Set<String> result) {
		double p = macroPrecision(gold, result);
		double r = macroRecall(gold, result);
		if (p == 0 || r == 0) {
			return 0;
		}
		double f1 = (2 * (p * r)) / (p + r);
		return f1;

	}

	public static void main(String[] args) {

		Set<String> gold = new HashSet<String>();
		// gold.add("MESH:D061325");
		// gold.add("MESH:D001943|MESH:D018567");
		// gold.add("MESH:D010051");
		// gold.add("MESH:D009369|MESH:D010051");
		// gold.add("MESH:D001943");
		String[] g = "MESH:D010534, MESH:D060831, MESH:D010051, MESH:D064420, MESH:D003967, MESH:D005185|MESH:D010051, MESH:D013280"
				.split(",");

		for (String string : g) {
			gold.add(string.trim());
		}

		String[] r = "MESH:D010534, MESH:D005185, MESH:D002277, MESH:D060831, MESH:D010051, MESH:D003967, MESH:D064420, MESH:D013280"
				.split(",");
		Set<String> result = new HashSet<String>();

		for (String string : r) {
			result.add(string.trim());
		}
		// result.add("MESH:D009369");
		// result.add("MESH:D001943");
		// result.add("MESH:D010051");
		// result.add("MESH:D061325");

		System.out.println("tp = " + getTruePositives(gold, result));
		System.out.println("fp = " + getFalsePositives(gold, result));
		System.out.println("fn = " + getFalseNegatives(gold, result));

		System.out.println(retainAllPrecision(gold, result));
		System.out.println(retainAllRecall(gold, result));
		System.out.println(macroPrecision(gold, result));
		System.out.println(macroRecall(gold, result));
		System.out.println(macroF1(gold, result));
	}

	private static Set<String> retainAllPrecision(final Set<String> gold, final Set<String> result) {
		Set<String> retainAll = new HashSet<String>();

		for (String s : gold) {
			if (result.contains(s)) {
				retainAll.add(s);
			}

			final String[] ids = s.split("\\|");
			if (ids.length != 1) {
				for (String id : ids) {
					if (result.contains(id)) {
						retainAll.add(id);
					}
				}
			}
		}
		return retainAll;
	}

	private static Set<String> retainAllRecall(final Set<String> gold, final Set<String> result) {
		Set<String> retainAll = new HashSet<String>();

		for (String s : gold) {
			if (result.contains(s)) {
				retainAll.add(s);
			}

		}
		return retainAll;
	}

}

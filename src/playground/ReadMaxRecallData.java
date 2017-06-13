package playground;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ReadMaxRecallData {

	static class Data {
		int dist;
		float sim;
		int tp;
		int fp;
		int fn;
		float microRecall;
		float microPrecision;
		float microF1Score;
		float macroRecall;
		float macroPrecision;
		float macroF1Score;
		long time;

		public Data(int dist, float sim, int tp, int fp, int fn, float microRecall, float microPrecision,
				float microF1Score, float macroRecall, float macroPrecision, float macroF1Score, long time) {
			this.dist = dist;
			this.sim = sim;
			this.tp = tp;
			this.fp = fp;
			this.fn = fn;
			this.microRecall = microRecall;
			this.microPrecision = microPrecision;
			this.microF1Score = microF1Score;
			this.macroRecall = macroRecall;
			this.macroPrecision = macroPrecision;
			this.macroF1Score = macroF1Score;
			this.time = time;
		}

		@Override
		public String toString() {

			return dist + "\t" + sim + "\t" + tp + "\t" + fp + "\t" + fn + "\t" + microF1Score + "\t" + microPrecision
					+ "\t" + microRecall + "\t" + macroF1Score + "\t" + macroPrecision + "\t" + macroRecall + "\t"
					+ time;

		}

	}

	public static void main(String[] args) throws IOException {

		// final String fileName = "res/luceneMaxRecall";
		// final String outFileName = "gen/luceneMaxRecall.csv";
		final String fileName = "res/jaccardMaxRecall";
		final String outFileName = "gen/jaccardMaxRecall.csv";
		// final String fileName = "res/levenshteinMaxRecall";
		// final String outFileName = "gen/levenshteinMaxRecall.csv";

		BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));

		String line = "";
		Data currentData = null;
		int dist = 0;
		float sim = 0;
		int tp = 0;
		int fp = 0;
		int fn = 0;
		float microRecall = 0;
		float microPrecision = 0;
		float microF1Score = 0;
		float macroRecall = 0;
		float macroPrecision = 0;
		float macroF1Score = 0;
		long time = 0;

		List<Data> datas = new ArrayList<>();

		while ((line = br.readLine()) != null) {

			if (line.startsWith("Dist")) {

				currentData = new Data(dist, sim, tp, fp, fn, microRecall, microPrecision, microF1Score, macroRecall,
						macroPrecision, macroF1Score, time);
				if (dist != 0)
					datas.add(currentData);

				dist = Integer.parseInt(line.split("=")[1].trim());

			}

			if (line.startsWith("Similarity")) {
				sim = Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("Time")) {
				time = Long.parseLong(line.split("=")[1].trim());
			}
			if (line.startsWith("tp")) {
				tp = (int) Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("fp")) {
				fp = (int) Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("fn")) {
				fn = (int) Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("Micro precision")) {
				microPrecision = Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("Micro recall")) {
				microRecall = Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("Micro F1")) {
				microF1Score = Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("Macro precision")) {
				macroPrecision = Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("Macro recall")) {
				macroRecall = Float.parseFloat(line.split("=")[1].trim());
			}
			if (line.startsWith("Macro F1")) {
				macroF1Score = Float.parseFloat(line.split("=")[1].trim());
			}
		}
		currentData = new Data(dist, sim, tp, fp, fn, microRecall, microPrecision, microF1Score, macroRecall,
				macroPrecision, macroF1Score, time);

		datas.add(currentData);

		PrintStream ps = new PrintStream(outFileName);

		String header = "dist\tsim\ttp\tfp\tfn\tmicroF1Score\tmicroPrecision\tmicroRecall\tmacroF1Score\tmacroPrecision\tmacroRecall\ttime";
		ps.println(header);

		datas.forEach(ps::println);
		ps.close();
	}

}

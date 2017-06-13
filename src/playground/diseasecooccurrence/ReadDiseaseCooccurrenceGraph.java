package playground.diseasecooccurrence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadDiseaseCooccurrenceGraph {

	/**
	 * DiseaseID, (DiseaseID,Count )
	 */
	public static Map<String, Map<String, Integer>> coocurrenceMap;

	public static void main(String[] args) throws IOException {

		System.out.println("Restore disease coocrrence data...");
		coocurrenceMap = (Map<String, Map<String, Integer>>) restoreData(
				"/home/hterhors/Workspace/BIREDiseaseNormalization/res/disease_cooccurrence.bin");
		System.out.println("done!");
		if (coocurrenceMap != null) {
			System.out.println("CoocurrenceMap size = " + coocurrenceMap.size());
		} else {

			coocurrenceMap = new HashMap<>(20000);
			Pattern p = Pattern.compile("((OMIM:|(MESH:(D|C)))[0-9]*)");
			AtomicInteger progress = new AtomicInteger(0);
			Files.readAllLines(
					new File("/home/hterhors/Workspace/BIREDiseaseNormalization/res/fullgraphdata/all.out").toPath())
					.forEach(l -> {
						Matcher m = p.matcher(l);
						Set<String> ids = new HashSet<>();
						int i = progress.incrementAndGet();
						if (i % 1000 == 0) {
							System.out.println(progress.get());
						}
						while (m.find()) {
							String id = m.group(0);
							ids.add(id);
							coocurrenceMap.putIfAbsent(id, new HashMap<>());
						}

						for (String id1 : ids) {
							for (String id2 : ids) {
								if (!id1.equals(id2))
									coocurrenceMap.get(id1).put(id2, coocurrenceMap.get(id1).getOrDefault(id2, 0) + 1);
							}
						}
					});

			writeData("/home/hterhors/Workspace/BIREDiseaseNormalization/res/DiseaseCooccurrence.bin", coocurrenceMap);
		}

		// System.out.println(coocurrenceMap.get("MESH:D011125"));

	}
	// Gold = [MESH:D011125, MESH:D015179]

	public static Object restoreData(final String filename) {
		Object data = null;
		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(filename);
			ObjectInputStream in;
			in = new ObjectInputStream(fileIn);
			data = in.readObject();
			in.close();
			fileIn.close();
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
		return data;
	}

	public static void writeData(final String filename, final Object data) {

		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(filename);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(data);
			out.close();
			fileOut.close();
		} catch (final Exception e) {
		}
	}
}

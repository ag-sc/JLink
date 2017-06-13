package playground;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import tokenization.Tokenizer;

public class RomanDiseaseRec {

	final public static Set<String> diseaseNames = new HashSet<>();

	public static void main(String[] args) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(new File("res/testset_diseases.tsv")));

		String line = "";

		while ((line = br.readLine()) != null) {

			final String diseaseSurfaceForm = Tokenizer.getTokenizedForm(line.split("\t")[5]);

			// if (diseaseSurfaceForm.length() <=
			// DictionaryLookupTemplateFlat.MIN_DISEASE_LENGHT)
			// continue;
			//
			// if
			// (DictionaryLookupTemplateFlat.upperPlusSize(diseaseSurfaceForm))
			// continue;

			diseaseNames.add(diseaseSurfaceForm);
		}
		br.close();

		// diseaseNames.forEach(System.out::println);

	}
}

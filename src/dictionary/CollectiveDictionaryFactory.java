package dictionary;

import dictionary.subdicts.ChemicalDatasetDictionary;
import dictionary.subdicts.ChemicalMedicDictionary;
import dictionary.subdicts.DiseaseChemicalDatasetDictionary;
import dictionary.subdicts.DiseaseDatasetDictionary;
import dictionary.subdicts.DiseaseMedicDictionary;
import main.JLink;
import main.Main;
import main.ParameterReader;
import main.param.Parameter;
import main.setting.EDataset;

public class CollectiveDictionaryFactory {

	static CollectiveDictionary diseaseDictionary = null;
	static CollectiveDictionary chemicalDictionary = null;
	static CollectiveDictionary diseaseChemicalDictionary = null;

	public static void main(String[] args) {
		Parameter parameter;

		if (args == null || args.length == 0) {
			parameter = ParameterReader.defaultParameters();
		} else {
			parameter = ParameterReader.readParametersFromCommandLine(args);
		}
		new JLink(Main.buildSettings(parameter));

		getChemicalInstance();
		// getDiseaseInstance();
		System.out.println(chemicalDictionary.getAllSurfaceForms().size());

	}

	public static CollectiveDictionary getInstance() {

		switch (JLink.type) {
		case Disease:
			return getDiseaseInstance();
		case Chemical:
			return getChemicalInstance();
		case Disease_Chemical:
			return getDiseaseChemicalInstance();
		}
		return null;
	}

	public static CollectiveDictionary getDiseaseInstance() {
		if (diseaseDictionary == null) {
			diseaseDictionary = new CollectiveDictionary();

			diseaseDictionary.addDictionary(new DiseaseMedicDictionary(5));

			/*
			 * NOTE: We remove this since on test set develop should be included
			 * in training data.
			 * 
			 * TODO: If one need distinction between train and development data
			 * (for ordering importance e.g.) distinguish here!
			 */
			// if (JLink.setting.testDataset == EDataset.TEST)
			// diseaseDictionary.addDictionary(new
			// DiseaseDatasetDictionary(EDataset.DEVELOP, 4));

			diseaseDictionary.addDictionary(new DiseaseDatasetDictionary(EDataset.TRAIN, 3));
			diseaseDictionary.build();

		}

		return diseaseDictionary;

	}

	public static CollectiveDictionary getChemicalInstance() {
		if (chemicalDictionary == null) {
			chemicalDictionary = new CollectiveDictionary();

			chemicalDictionary.addDictionary(new ChemicalMedicDictionary(5));
			// if (JLink.setting.testDataset == EDataset.TEST)
			// chemicalDictionary.addDictionary(new
			// ChemicalDatasetDictionary(EDataset.DEVELOP, 4));
			chemicalDictionary.addDictionary(new ChemicalDatasetDictionary(EDataset.TRAIN, 3));

			chemicalDictionary.build();

		}

		return chemicalDictionary;
	}

	public static CollectiveDictionary getDiseaseChemicalInstance() {
		if (diseaseChemicalDictionary == null) {
			diseaseChemicalDictionary = new CollectiveDictionary();

			diseaseChemicalDictionary.addDictionary(new DiseaseMedicDictionary(6));
			if (JLink.setting.testDataset == EDataset.TEST) {
				diseaseChemicalDictionary.addDictionary(new DiseaseChemicalDatasetDictionary(EDataset.DEVELOP, 5));
			}
			diseaseChemicalDictionary.addDictionary(new DiseaseChemicalDatasetDictionary(EDataset.TRAIN, 5));
			diseaseChemicalDictionary.addDictionary(new ChemicalMedicDictionary(4));

			diseaseChemicalDictionary.build();

		}

		return diseaseChemicalDictionary;
	}

}

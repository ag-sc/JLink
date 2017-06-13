package corpus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import main.JLink;
import main.setting.ECorpus;
import main.setting.EDataset;

public class DatasetConfig {

	public static final Properties PROPERTIES = new Properties();

	private static void readProperties() {

		if (!PROPERTIES.isEmpty())
			return;

		try {

			switch (JLink.type) {
			case Chemical:
				if (JLink.setting.corpus == ECorpus.BC5)
					if (JLink.setting.testDataset == EDataset.DEVELOP)
						PROPERTIES.load(new FileInputStream("config/BC5_chemicals_train_dataset.properties"));
					else {
						PROPERTIES.load(new FileInputStream("config/BC5_chemicals_test_dataset.properties"));
					}

				break;
			case Disease:
				if (JLink.setting.corpus == ECorpus.BC5)
					if (JLink.setting.testDataset == EDataset.DEVELOP)
						PROPERTIES.load(new FileInputStream("config/BC5_origin_dataset.properties"));
					else {
						PROPERTIES.load(new FileInputStream("config/BC5_test_dataset.properties"));
					}

				break;
			case Disease_Chemical:

				if (JLink.setting.corpus == ECorpus.BC5)
					if (JLink.setting.testDataset == EDataset.DEVELOP)
						PROPERTIES.load(new FileInputStream("config/BC5_both_train_dataset.properties"));
					else {
						PROPERTIES.load(new FileInputStream("config/BC5_both_test_dataset.properties"));
					}
				break;

			}

			if (JLink.setting.corpus == ECorpus.NCBI)
				if (JLink.setting.testDataset == EDataset.DEVELOP)
					PROPERTIES.load(new FileInputStream("config/BC2_train_dataset.properties"));
				else {
					PROPERTIES.load(new FileInputStream("config/BC2_test_dataset.properties"));
				}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLink.log.info("Loaded properties: " + PROPERTIES);
	}

	public static String getDiseaseMedicFilepath() {
		readProperties();
		return PROPERTIES.getProperty("DISEASE_MEDIC");
	}

	public static String getChemicalMedicFilepath() {
		readProperties();
		return PROPERTIES.getProperty("CHEMICAL_MEDIC");
	}

	public static String getDevelopmentCorpusPath() {
		readProperties();
		return PROPERTIES.getProperty("DEV_DIRPATH");
	}

	public static String getTestCorpusPath() {
		readProperties();
		return PROPERTIES.getProperty("TEST_DIRPATH");
	}

	public static String getTrainCorpusPath() {
		readProperties();
		return PROPERTIES.getProperty("TRAIN_DIRPATH");
	}

	public static String getPosTaggerFile() {
		readProperties();
		return PROPERTIES.getProperty("POS_TAGGER_MODEL");
	}

}

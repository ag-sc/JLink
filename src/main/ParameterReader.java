package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import main.param.Parameter;
import main.setting.CandidateRetrievalSetting;
import main.setting.ECorpus;
import main.setting.EDataset;
import main.setting.EEvaluationMode;
import main.setting.EScorer;
import main.setting.TemplateSetting;

public class ParameterReader {

	public static final Properties PARAMS = new Properties();

	public static final String EPOCH_KEY = "EPOCH";
	public static final String ALPHA_KEY = "ALPHA";
	public static final String ROOT_DIRECTORY_KEY = "ROOT_DIRECTORY";

	public static final String SCORER_TYPE_KEY = "SCORER_TYPE";

	public static final String INCLUDE_LUCENE_CANDIDATE_RETRIEVAL_KEY = "INCLUDE_LUCENE_CANDIDATE_RETRIEVAL";
	public static final String INCLUDE_JACCARD_CANDIDATE_RETRIEVAL_KEY = "INCLUDE_JACCARD_CANDIDATE_RETRIEVAL";
	public static final String INCLUDE_LEVENSHTEIN_CANDIDATE_RETRIEVAL_KEY = "INCLUDE_LEVENSHTEIN_CANDIDATE_RETRIEVAL";

	private static final String INCLUDE_DICTIONARY_LOOKUP_TEMPLATE_KEY = "INCLUDE_DICTIONARY_LOOKUP_TEMPLATE";
	private static final String INCLUDE_NUMBER_OF_TOKENS_TEMPLATE_KEY = "INCLUDE_NUMBER_OF_TOKENS_TEMPLATE";
	private static final String INCLUDE_INTERNAL_MENTION_TOKENTEMPLATE_KEY = "INCLUDE_INTERNAL_MENTION_TOKENTEMPLATE";
	private static final String INCLUDE_TOKEN_CONTEXT_TEMPLATE_KEY = "INCLUDE_TOKEN_CONTEXT_TEMPLATE";
	private static final String INCLUDE_CONCEPT_CONTEXT_TEMPLATE_KEY = "INCLUDE_CONCEPT_CONTEXT_TEMPLATE";
	private static final String INCLUDE_SYNTACTIC_TRANSFORMATION_KEY = "INCLUDE_SYNTACTIC_TRANSFORMATION";
	private static final String INCLUDE_ANNOTATION_TEXT_TEMPLATE_KEY = "INCLUDE_ANNOTATION_TEXT_TEMPLATE";
	private static final String INCLUDE_CADIDATE_RETRIEVAL_TEMPLATE_KEY = "INCLUDE_CADIDATE_RETRIEVAL_TEMPLATE";
	private static final String INCLUDE_ABBREVIATION_TEMPLATE_KEY = "INCLUDE_ABBREVIATION_TEMPLATE";
	private static final String INCLUDE_MORPHOLOGICAL_TRANSFORMATION_TEMPLATE_KEY = "INCLUDE_MORPHOLOGICAL_TRANSFORMATION_TEMPLATE";
	private static final String INCLUDE_TRAINING_PRIOR_TEMPLATE_KEY = "INCLUDE_TRAINING_PRIOR_TEMPLATE";

	private static final String TEST_DATASET_KEY = "TEST_DATASET";
	private static final String TRAINING_DATA_KEY = "TRAINING_DATA";
	private static final String CORPUS_TYPE_KEY = "CORPUS_TYPE";
	private static final String EVALUATION_MODE_KEY = "EVALUATION_MODE";

	private static final String PERSONAL_NOTE_KEY = "PERSONAL_NOTE";

	private static void readProperties(final String parameterFileName) throws FileNotFoundException, IOException {

		if (!PARAMS.isEmpty())
			return;

		PARAMS.load(new FileInputStream(parameterFileName));

		JLink.log.info("Loaded parameter: " + PARAMS);
	}

	public static Parameter readParametersFromFile(String parameterFileName) throws FileNotFoundException, IOException {

		readProperties(parameterFileName);

		final String rootDirectory = PARAMS.getProperty(ROOT_DIRECTORY_KEY, "./");

		final int epochs = Integer.parseInt(PARAMS.getProperty(EPOCH_KEY, "-1"));

		final boolean includeLuceneCandidateRetrieval = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_LUCENE_CANDIDATE_RETRIEVAL_KEY, "false"));
		final boolean includeJaccardCandidateRetrieval = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_JACCARD_CANDIDATE_RETRIEVAL_KEY, "false"));
		final boolean includeLevenshteinCandidateRetrieval = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_LEVENSHTEIN_CANDIDATE_RETRIEVAL_KEY, "false"));

		final EScorer scorerType = EScorer
				.valueOf(PARAMS.getProperty(SCORER_TYPE_KEY, EScorer.EXP.name()).toUpperCase());

		final double alpha = Double.parseDouble(PARAMS.getProperty(ALPHA_KEY, "0"));

		final String personalNote = PARAMS.getProperty(PERSONAL_NOTE_KEY, "");

		final EDataset testDataset = EDataset
				.valueOf(PARAMS.getProperty(TEST_DATASET_KEY, EDataset.DEVELOP.name()).toUpperCase());
		final EDataset trainDataset = EDataset
				.valueOf(PARAMS.getProperty(TRAINING_DATA_KEY, EDataset.TRAIN.name()).toUpperCase());
		final ECorpus corpus = ECorpus.valueOf(PARAMS.getProperty(CORPUS_TYPE_KEY, null).toUpperCase());
		final EEvaluationMode evaluationMode = EEvaluationMode
				.valueOf(PARAMS.getProperty(EVALUATION_MODE_KEY, EEvaluationMode.TRAIN_TEST.name()).toUpperCase());

		CandidateRetrievalSetting candidateRetrievalSetting = new CandidateRetrievalSetting(
				includeLuceneCandidateRetrieval, includeJaccardCandidateRetrieval,
				includeLevenshteinCandidateRetrieval);

		final boolean includeDictionaryLookUpTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_DICTIONARY_LOOKUP_TEMPLATE_KEY, "false"));
		final boolean includeNumberOfTokensTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_NUMBER_OF_TOKENS_TEMPLATE_KEY, "false"));
		final boolean includeInternalMentionTokenTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_INTERNAL_MENTION_TOKENTEMPLATE_KEY, "false"));
		final boolean includeTokenContextTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_TOKEN_CONTEXT_TEMPLATE_KEY, "false"));
		final boolean includeConceptContextTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_CONCEPT_CONTEXT_TEMPLATE_KEY, "false"));
		final boolean includeSyntacticTransformation = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_SYNTACTIC_TRANSFORMATION_KEY, "false"));
		final boolean includeAnnotationTextTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_ANNOTATION_TEXT_TEMPLATE_KEY, "false"));
		final boolean includeCadidateRetrievalTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_CADIDATE_RETRIEVAL_TEMPLATE_KEY, "false"));
		final boolean includeAbbreviationTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_ABBREVIATION_TEMPLATE_KEY, "false"));
		final boolean includeMorphologicalTransformationTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_MORPHOLOGICAL_TRANSFORMATION_TEMPLATE_KEY, "false"));
		final boolean includeTrainingPriorTemplate = Boolean
				.parseBoolean(PARAMS.getProperty(INCLUDE_TRAINING_PRIOR_TEMPLATE_KEY, "false"));

		TemplateSetting templateSetting = new TemplateSetting(includeDictionaryLookUpTemplate,
				includeNumberOfTokensTemplate, includeInternalMentionTokenTemplate, includeTokenContextTemplate,
				includeConceptContextTemplate, includeSyntacticTransformation, includeAnnotationTextTemplate,
				includeCadidateRetrievalTemplate, includeAbbreviationTemplate,
				includeMorphologicalTransformationTemplate, includeTrainingPriorTemplate);
		return new Parameter(candidateRetrievalSetting, templateSetting, rootDirectory, epochs, alpha, scorerType,
				corpus, trainDataset, testDataset, evaluationMode, personalNote);
	}

	public static Parameter readParametersFromCommandLine(String[] args) {

		JLink.log.info("First argument: Root directory.");
		JLink.log.info("Second argument: Number of epochs.(100)");
		JLink.log.info("Third argument: Template Bitset-Setting.(1 or 0)");
		JLink.log.info("Fourth argument: Include Lucene as Candidate Retrieval.(true)");
		JLink.log.info("Fifth argument: Include Jaccard (3-grams) as Candidate Retrieval.(false)");
		JLink.log.info("Sixth argument: Include Levensthein as Candidate Retrieval.(false)");
		JLink.log.info("Seventh argument: Scorer: EXP,LINEAR,SOFTPLUS.(EXP)");
		JLink.log.info("Eightth argument: learning rate alpha.(0.05)");

		String rootDirectory = null;

		if (args.length > 0)
			rootDirectory = args[0];

		int epochs = -1;

		if (args.length > 1)
			epochs = Integer.parseInt(args[1]);

		String templateSettingString = null;

		if (args.length > 2)
			templateSettingString = args[2];

		boolean includeLuceneCandidateRetrieval = false;
		boolean includeJaccardCandidateRetrieval = false;
		boolean includeLevenshteinCandidateRetrieval = false;

		if (args.length > 3)
			includeLuceneCandidateRetrieval = Boolean.valueOf(args[3]);
		if (args.length > 4)
			includeJaccardCandidateRetrieval = Boolean.valueOf(args[4]);
		if (args.length > 5)
			includeLevenshteinCandidateRetrieval = Boolean.valueOf(args[5]);

		EScorer scorerType = null;

		if (args.length > 6)
			scorerType = EScorer.valueOf(args[6].toUpperCase());

		double alpha = 0;

		if (args.length > 7)
			alpha = Double.parseDouble(args[7]);

		String personalNote = null;

		if (args.length > 8)
			personalNote = args[8].trim();

		/*
		 * TODO: AS PARAM
		 */
		EDataset testDataset = EDataset.DEVELOP;
		EDataset trainDataset = EDataset.TRAIN;
		ECorpus corpus = ECorpus.BC5;
		EEvaluationMode evaluationMode = EEvaluationMode.TRAIN_TEST;

		boolean[] settingArray = new boolean[templateSettingString.toCharArray().length];

		for (int j = 0; j < templateSettingString.toCharArray().length; j++) {
			if (templateSettingString.toCharArray()[j] == '0') {
				settingArray[j] = false;

			}
			if (templateSettingString.toCharArray()[j] == '1') {
				settingArray[j] = true;
			}
		}

		boolean includeDictionaryLookUpTemplate = settingArray[0];
		boolean includeNumberOfTokensTemplate = settingArray[1];
		boolean includeInternalMentionTokenTemplate = settingArray[2];
		boolean includeTokenContextTemplate = settingArray[3];
		boolean includeConceptContextTemplate = settingArray[4];
		boolean includeSyntacticTransformation = settingArray[5];
		boolean includeAnnotationTextTemplate = settingArray[6];
		boolean includeCadidateRetrievalTemplate = settingArray[7];
		boolean includeAbbreviationTemplate = settingArray[8];
		boolean includeMorphologicalTransformationTemplate = settingArray[9];
		boolean includeTrainingPriorTemplate = settingArray[10];

		CandidateRetrievalSetting candidateRetrievalSetting = new CandidateRetrievalSetting(
				includeLuceneCandidateRetrieval, includeJaccardCandidateRetrieval,
				includeLevenshteinCandidateRetrieval);

		TemplateSetting templateSetting = new TemplateSetting(includeDictionaryLookUpTemplate,
				includeNumberOfTokensTemplate, includeInternalMentionTokenTemplate, includeTokenContextTemplate,
				includeConceptContextTemplate, includeSyntacticTransformation, includeAnnotationTextTemplate,
				includeCadidateRetrievalTemplate, includeAbbreviationTemplate,
				includeMorphologicalTransformationTemplate, includeTrainingPriorTemplate);
		return new Parameter(candidateRetrievalSetting, templateSetting, rootDirectory, epochs, alpha, scorerType,
				corpus, trainDataset, testDataset, evaluationMode, personalNote);
	}

	public static Parameter defaultParameters() {
		final String personalNote = "";

		final String modelRootDirectory = "gen/";
		final int epochs = 130;
		final double alpha = 0.06;
		final EScorer scorerType = EScorer.EXP;
		final ECorpus corpus = ECorpus.BC5;
		final EDataset trainDataset = EDataset.TRAIN;
		final EDataset testDataset = EDataset.TEST;
		final EEvaluationMode evaluationMode = EEvaluationMode.TRAIN_TEST;

		boolean includeLuceneCandidateRetrieval = true;
		boolean includeJaccardCandidateRetrieval = false;
		boolean includeLevenshteinCandidateRetrieval = false;

		CandidateRetrievalSetting candidateRetrievalSetting = new CandidateRetrievalSetting(
				includeLuceneCandidateRetrieval, includeJaccardCandidateRetrieval,
				includeLevenshteinCandidateRetrieval);

		boolean includeDictionaryLookUpTemplate = true;
		boolean includeNumberOfTokensTemplate = true;
		boolean includeInternalMentionTokenTemplate = true;
		boolean includeTokenContextTemplate = true;
		boolean includeConceptContextTemplate = false;
		boolean includeSyntacticTransformation = true;
		boolean includeAnnotationTextTemplate = true;
		boolean includeCadidateRetrievalTemplate = true;
		boolean includeAbbreviationTemplate = true;
		boolean includeMorphologicalTransformationTemplate = false;
		boolean includeTrainingPriorTemplate = true;

		TemplateSetting templateSetting = new TemplateSetting(includeDictionaryLookUpTemplate,
				includeNumberOfTokensTemplate, includeInternalMentionTokenTemplate, includeTokenContextTemplate,
				includeConceptContextTemplate, includeSyntacticTransformation, includeAnnotationTextTemplate,
				includeCadidateRetrievalTemplate, includeAbbreviationTemplate,
				includeMorphologicalTransformationTemplate, includeTrainingPriorTemplate);

		return new Parameter(candidateRetrievalSetting, templateSetting, modelRootDirectory, epochs, alpha, scorerType,
				corpus, trainDataset, testDataset, evaluationMode, personalNote);
	}

}

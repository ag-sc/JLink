package main.setting;

import java.util.Map.Entry;

import candidateretrieval.ICandidateRetrieval;
import templates.AbstractTemplate;

/**
 * Settings class for the current run. This class contains all needed
 * information about the run, including some personal notes.
 * 
 * @author hterhors
 *
 *         Oct 11, 2016
 */
public class Setting {

	public String getShortString() {
		return shortString;
	}

	public final long version;

	public final double alpha;
	public final EScorer scorerType;
	public final ECorpus corpus;
	public final EDataset trainDataset;
	public final EDataset testDataset;

	public final boolean runInDebug;

	public final CandidateRetrievalSetting candidateRetrievalSetting;

	public final TemplateSetting templateSetting;
	private final String shortString;
	public final String personalNotes;

	public final int epochs;
	public final String modelRootDirectory;
	public final EEvaluationMode evaluationMode;

	public Setting(long version, String personalNotes, double alpha, EScorer scorerType, ECorpus corpus,
			EDataset trainDataset, EDataset testDataset, boolean runInDebug,
			CandidateRetrievalSetting candidateRetrievalSetting, TemplateSetting templateSetting, int epochs,
			String modelRootDirectory, EEvaluationMode evaluationMode) {
		this.version = version;
		this.personalNotes = personalNotes;
		this.alpha = alpha;
		this.scorerType = scorerType;
		this.corpus = corpus;
		this.trainDataset = trainDataset;
		this.testDataset = testDataset;
		this.runInDebug = runInDebug;
		this.candidateRetrievalSetting = candidateRetrievalSetting;
		this.templateSetting = templateSetting;
		this.shortString = getSettingsAsShortString();
		this.epochs = epochs;
		this.modelRootDirectory = modelRootDirectory;
		this.evaluationMode = evaluationMode;
	}

	private String getSettingsAsShortString() {
		StringBuffer sb = new StringBuffer();
		sb.append("v" + version);
		sb.append("_");
		sb.append(alpha);
		sb.append("_");
		sb.append(scorerType);
		sb.append("_");

		for (Entry<Class<? extends ICandidateRetrieval>, Boolean> retrieval : candidateRetrievalSetting.setting
				.entrySet()) {
			sb.append(retrieval.getValue() ? "1" : "0");
		}
		sb.append("_");
		for (Entry<Class<? extends AbstractTemplate<?, ?, ?>>, Boolean> retrieval : templateSetting.setting
				.entrySet()) {
			sb.append(retrieval.getValue() ? "1" : "0");
		}
		// sb.append(templateSetting.includeDictionaryLookUpTemplate ? "1" :
		// "0");
		// sb.append(templateSetting.includeNumberOfTokensTemplate ? "1" : "0");
		// sb.append(templateSetting.includeInternalMentionTokenTemplate ? "1" :
		// "0");
		// sb.append(templateSetting.includeTokenContextTemplate ? "1" : "0");
		// sb.append(templateSetting.includeConceptContextTemplate ? "1" : "0");
		// sb.append(templateSetting.includeSyntacticTransformation ? "1" :
		// "0");
		// sb.append(templateSetting.includeAnnotationTextTemplate ? "1" : "0");
		// sb.append(templateSetting.includeCadidateRetrievalTemplate ? "1" :
		// "0");
		// sb.append(templateSetting.includeAbbreviationTemplate ? "1" : "0");
		// sb.append(templateSetting.includeMorphologicalTransformationTemplate
		// ? "1" : "0");
		// sb.append(templateSetting.includeTrainingPriorTemplate ? "1" : "0");

		return sb.toString();
	}

	@Override
	public String toString() {
		return "Setting [\nversion=" + version + ", \nalpha=" + alpha + ", \nscorerType=" + scorerType + ", \ncorpus="
				+ corpus + ", \ntrainDataset=" + trainDataset + ", \ntestDataset=" + testDataset + ", \nrunInDebug="
				+ runInDebug + ", \ncandidateRetrievalSetting=" + candidateRetrievalSetting + ", \ntemplateSetting="
				+ templateSetting + ", \nshortString=" + shortString + ", \npersonalNotes=" + personalNotes + "]";
	}

}

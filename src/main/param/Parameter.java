package main.param;

import main.setting.CandidateRetrievalSetting;
import main.setting.ECorpus;
import main.setting.EDataset;
import main.setting.EEvaluationMode;
import main.setting.EScorer;
import main.setting.TemplateSetting;

public class Parameter {

	public final CandidateRetrievalSetting candidateRetrievalSetting;

	public final TemplateSetting templateSetting;

	public final int epochs;
	public final String modelRootDirectory;
	public final double alpha;
	public final EScorer scorerType;
	public final ECorpus corpus;
	public final EDataset trainDataset;
	public final EDataset testDataset;
	public final EEvaluationMode evaluationMode;

	public final String personalNotes;

	public Parameter(CandidateRetrievalSetting candidateRetrievalSetting, TemplateSetting templateSetting,
			String rootDirectory, int epochs, double alpha, EScorer scorerType, ECorpus corpus, EDataset trainDataset,
			EDataset testDataset, EEvaluationMode evaluationMode, String personalNotes) {
		this.candidateRetrievalSetting = candidateRetrievalSetting;
		this.templateSetting = templateSetting;
		this.epochs = epochs;
		this.modelRootDirectory = rootDirectory;
		this.alpha = alpha;
		this.scorerType = scorerType;
		this.corpus = corpus;
		this.trainDataset = trainDataset;
		this.testDataset = testDataset;
		this.evaluationMode = evaluationMode;
		this.personalNotes = personalNotes;
	}

	@Override
	public String toString() {
		return "Parameter [candidateRetrievalSetting=" + candidateRetrievalSetting + ", templateSetting="
				+ templateSetting + ", epochs=" + epochs + ", rootDirectory=" + modelRootDirectory + ", alpha=" + alpha
				+ ", scorerType=" + scorerType + ", corpus=" + corpus + ", trainDataset=" + trainDataset
				+ ", testDataset=" + testDataset + ", evaluationMode=" + evaluationMode + ", personalNotes="
				+ personalNotes + "]";
	}

}

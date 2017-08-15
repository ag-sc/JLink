package main.setting;

import java.util.LinkedHashMap;
import java.util.Map;

import templates.AbbreviationTemplate;
import templates.AbstractTemplate;
import templates.AnnotationTextTemplate;
import templates.CadidateRetrievalTemplate;
import templates.ConceptContextTemplate;
import templates.DictionaryLookUpTemplate;
import templates.InternalMentionTokenTemplate;
import templates.MorphologicalTransformationTemplate;
import templates.NumberOfTokensTemplate;
import templates.SemanticTransformationTemplate;
import templates.TokenContextTemplate;
import templates.TrainingPriorTemplate;

public class TemplateSetting {

	public final Map<Class<? extends AbstractTemplate<?, ?, ?>>, Boolean> setting;

	public TemplateSetting(boolean includeDictionaryLookUpTemplate, boolean includeNumberOfTokensTemplate,
			boolean includeInternalMentionTokenTemplate, boolean includeTokenContextTemplate,
			boolean includeConceptContextTemplate, boolean includeSyntacticTransformation,
			boolean includeAnnotationTextTemplate, boolean includeCadidateRetrievalTemplate,
			boolean includeAbbreviationTemplate, boolean includeMorphologicalTransformationTemplate,
			boolean includeTrainingPriorTemplate) {

		this.setting = new LinkedHashMap<>();

		this.setting.put(DictionaryLookUpTemplate.class, includeDictionaryLookUpTemplate);
		this.setting.put(NumberOfTokensTemplate.class, includeNumberOfTokensTemplate);
		this.setting.put(InternalMentionTokenTemplate.class, includeInternalMentionTokenTemplate);
		this.setting.put(TokenContextTemplate.class, includeTokenContextTemplate);
		this.setting.put(ConceptContextTemplate.class, includeConceptContextTemplate);
		this.setting.put(SemanticTransformationTemplate.class, includeSyntacticTransformation);
		this.setting.put(AnnotationTextTemplate.class, includeAnnotationTextTemplate);
		this.setting.put(CadidateRetrievalTemplate.class, includeCadidateRetrievalTemplate);
		this.setting.put(AbbreviationTemplate.class, includeAbbreviationTemplate);
		this.setting.put(MorphologicalTransformationTemplate.class, includeMorphologicalTransformationTemplate);
		this.setting.put(TrainingPriorTemplate.class, includeTrainingPriorTemplate);

	}

	@Override
	public String toString() {
		return "TemplateSetting [setting=" + setting + "]";
	}

	public boolean[] getBinarySetting() {

		boolean[] arr = new boolean[setting.size()];

		int index = 0;
		for (boolean b : setting.values()) {
			arr[index++] = b;
		}

		return arr;
	}

}

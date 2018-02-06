package org.molgenis.questionnaires.response;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.meta.QuestionnaireStatus;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_QuestionnaireResponse.class)
public abstract class QuestionnaireResponse
{
	public abstract String getName();

	public abstract String getLabel();

	@Nullable
	public abstract String getDescription();

	public abstract QuestionnaireStatus getStatus();

	public abstract Object getId();

	public static QuestionnaireResponse create(Questionnaire questionnaire)
	{
		return new AutoValue_QuestionnaireResponse(questionnaire.getEntityType().getId(), questionnaire.getLabel(),
				questionnaire.getDescription(), questionnaire.getStatus(), questionnaire.getIdValue());
	}
}

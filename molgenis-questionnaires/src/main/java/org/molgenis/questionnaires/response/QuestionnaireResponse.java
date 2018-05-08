package org.molgenis.questionnaires.response;

import com.google.auto.value.AutoValue;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.meta.QuestionnaireStatus;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_QuestionnaireResponse.class)
@SuppressWarnings("squid:S1610")
public abstract class QuestionnaireResponse
{
	public abstract String getId();

	public abstract String getLabel();

	@Nullable
	public abstract String getDescription();

	public abstract QuestionnaireStatus getStatus();

	public static QuestionnaireResponse create(String id, String label, String description, QuestionnaireStatus status)
	{
		return new AutoValue_QuestionnaireResponse(id, label, description, status);
	}

	public static QuestionnaireResponse create(Questionnaire questionnaire)
	{
		return create(String.valueOf(questionnaire.getIdValue()) , questionnaire.getLabel(), questionnaire.getDescription(), questionnaire.getStatus());
	}
}

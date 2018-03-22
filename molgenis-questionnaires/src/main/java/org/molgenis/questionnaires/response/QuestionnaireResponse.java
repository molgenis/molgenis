package org.molgenis.questionnaires.response;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;
import org.molgenis.questionnaires.meta.QuestionnaireStatus;

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
}

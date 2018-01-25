package org.molgenis.questionnaires;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

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

	public static QuestionnaireResponse create(String name, String label, String description, QuestionnaireStatus status, Object id)
	{
		return new AutoValue_QuestionnaireResponse(name, label, description, status, id);
	}
}

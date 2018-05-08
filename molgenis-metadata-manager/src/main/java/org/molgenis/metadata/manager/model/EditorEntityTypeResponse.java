package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeResponse.class)
public abstract class EditorEntityTypeResponse
{
	abstract EditorEntityType getEntityType();

	abstract List<String> getLanguageCodes();

	public static EditorEntityTypeResponse create(EditorEntityType entityType, List<String> languageCodes)
	{
		return new AutoValue_EditorEntityTypeResponse(entityType, languageCodes);
	}
}

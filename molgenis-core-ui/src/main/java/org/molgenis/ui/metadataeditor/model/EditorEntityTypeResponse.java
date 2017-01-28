package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeResponse.class)
public abstract class EditorEntityTypeResponse
{
	abstract EditorEntityType getEntityType();

	abstract ImmutableList<String> getLanguageCodes();

	public static EditorEntityTypeResponse create(EditorEntityType entityType, ImmutableList<String> languageCodes)
	{
		return new AutoValue_EditorEntityTypeResponse(entityType, languageCodes);
	}
}

package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttributeResponse.class)
public abstract class EditorAttributeResponse
{
	abstract EditorAttribute getAttribute();

	abstract List<String> getLanguageCodes();

	public static EditorAttributeResponse create(EditorAttribute attribute, List<String> languageCodes)
	{
		return new AutoValue_EditorAttributeResponse(attribute, languageCodes);
	}
}

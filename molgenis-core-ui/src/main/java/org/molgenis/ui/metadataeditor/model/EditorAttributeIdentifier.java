package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttributeIdentifier.class)
public abstract class EditorAttributeIdentifier
{
	public abstract String getId();

	@Nullable
	public abstract String getLabel();

	public static EditorAttributeIdentifier create(String id, @Nullable String label)
	{
		return new AutoValue_EditorAttributeIdentifier(id, label);
	}
}

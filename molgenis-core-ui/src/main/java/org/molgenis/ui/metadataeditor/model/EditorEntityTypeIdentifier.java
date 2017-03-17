package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeIdentifier.class)
public abstract class EditorEntityTypeIdentifier
{
	public abstract String getId();

	@Nullable
	public abstract String getLabel();

	public static EditorEntityTypeIdentifier create(String id, @Nullable String label)
	{
		return new AutoValue_EditorEntityTypeIdentifier(id, label);
	}
}

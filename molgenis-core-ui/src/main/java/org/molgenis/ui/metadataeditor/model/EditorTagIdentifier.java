package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorTagIdentifier.class)
public abstract class EditorTagIdentifier
{
	public abstract String getId();

	public abstract String getLabel();

	public static EditorTagIdentifier create(String id, String label)
	{
		return new AutoValue_EditorTagIdentifier(id, label);
	}
}

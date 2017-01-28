package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorPackage.class)
public abstract class EditorPackage
{
	abstract String getId();

	@Nullable
	abstract String getLabel();

	public static EditorPackage create(String id, @Nullable String label)
	{
		return new AutoValue_EditorPackage(id, label);
	}
}

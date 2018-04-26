package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorPackageIdentifier.class)
public abstract class EditorPackageIdentifier
{
	public abstract String getId();

	@Nullable
	public abstract String getLabel();

	public static EditorPackageIdentifier create(String id, @Nullable String label)
	{
		return new AutoValue_EditorPackageIdentifier(id, label);
	}
}

package org.molgenis.searchall.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_PackageResult.class)
public abstract class PackageResult
{
	abstract String getLabel();

	@Nullable
	abstract String getDescription();

	public static PackageResult create(String label, String description)
	{
		return new AutoValue_PackageResult(label, description);
	}
}

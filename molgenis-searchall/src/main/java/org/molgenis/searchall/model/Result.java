package org.molgenis.searchall.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Result.class)
public abstract class Result
{
	public abstract List<EntityTypeResult> getEntityTypes();

	public abstract List<PackageResult> getPackages();

	public static Result create(List<EntityTypeResult> entityTypes, List<PackageResult> packages)
	{
		return new AutoValue_Result(entityTypes, packages);
	}
}

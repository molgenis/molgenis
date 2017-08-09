package org.molgenis.data.rest.v2;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.data.meta.model.Package;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Result.class)
public abstract class Result
{
	public abstract List<EntityTypeResult> getEntityTypeResults();

	public abstract List<Package> getPackages();

	public static Result create(List<EntityTypeResult> entityTypeResults, List<Package> packages)
	{
		return new AutoValue_Result(entityTypeResults, packages);
	}
}

package org.molgenis.model.registry.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

/**
 * @author sido
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_ModelRegistrySearch.class)
public abstract class ModelRegistrySearch
{
	public static ModelRegistrySearch create(String query, int offset, int num, int total,
			List<ModelRegistryPackage> packages)
	{
		return new AutoValue_ModelRegistrySearch(query, offset, num, total, packages);
	}

	@SuppressWarnings("unused")
	public abstract String getQuery();

	@SuppressWarnings("unused")
	public abstract int getOffset();

	@SuppressWarnings("unused")
	public abstract int getNum();

	@SuppressWarnings("unused")
	public abstract int getTotal();

	@SuppressWarnings("unused")
	public abstract List<ModelRegistryPackage> getPackages();

}

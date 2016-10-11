package org.molgenis.bbmri.directory.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Query.class)
public abstract class Query
{
	public abstract List<Collection> getCollections();

	public abstract Filter getFilters();

	public static Query createQuery(List<Collection> collections, Filter filter)
	{
		return new AutoValue_Query(collections, filter);
	}
}

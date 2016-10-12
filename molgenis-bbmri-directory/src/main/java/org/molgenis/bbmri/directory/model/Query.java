package org.molgenis.bbmri.directory.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Query.class)
public abstract class Query
{
	public abstract URL getURL();

	public abstract List<Collection> getCollections();

	public abstract Filter getFilters();

	public abstract NToken getNToken();

	public static Query createQuery(URL url, List<Collection> collections, Filter filter, NToken nToken)
	{
		return new AutoValue_Query(url, collections, filter, nToken);
	}
}

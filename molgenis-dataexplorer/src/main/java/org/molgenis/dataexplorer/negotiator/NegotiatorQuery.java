package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NegotiatorQuery.class)
public abstract class NegotiatorQuery
{
	public abstract String getURL();

	public abstract List<Collection> getCollections();

	public abstract String getHumanReadable();

	@Nullable
	public abstract String getnToken();

	public static NegotiatorQuery create(String url, List<Collection> collections, String humanReadable,
			String nToken)
	{
		return new AutoValue_NegotiatorQuery(url, collections, humanReadable, nToken);
	}
}
package org.molgenis.dataexplorer.negotiator;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NegotiatorRequest.class)
public abstract class NegotiatorRequest
{
	public abstract String getURL();

	public abstract String getEntityId();

	public abstract String getRsql();

	public abstract String getHumanReadable();

	@Nullable
	public abstract String getnToken();

	public static NegotiatorRequest create(String url, String entityId, String rsql, String humanReadable,
			String nToken)
	{
		return new AutoValue_NegotiatorRequest(url, entityId, rsql, humanReadable, nToken);
	}
}
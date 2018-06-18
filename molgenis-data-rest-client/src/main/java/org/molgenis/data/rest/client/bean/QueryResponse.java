package org.molgenis.data.rest.client.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
@AutoGson(autoValueClass = AutoValue_QueryResponse.class)
@SuppressWarnings("squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class QueryResponse
{
	public static QueryResponse create(String href, int start, int num, int total, String prevHref, String nextHref,
			List<Map<String, Object>> items)
	{
		return new AutoValue_QueryResponse(href, start, num, total, prevHref, nextHref, items);
	}

	public abstract String getHref();

	public abstract int getStart();

	public abstract int getNum();

	public abstract int getTotal();

	@Nullable
	public abstract String getPrevHref();

	@Nullable
	public abstract String getNextHref();

	public abstract List<Map<String, Object>> getItems();
}
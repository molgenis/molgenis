package org.molgenis.data.rest.v2;

import static java.util.Objects.requireNonNull;

public class ResourcesResponseV2
{
	private final String href;

	public ResourcesResponseV2(String href)
	{
		this.href = requireNonNull(href);
	}

	public String getHref()
	{
		return this.href;
	}
}

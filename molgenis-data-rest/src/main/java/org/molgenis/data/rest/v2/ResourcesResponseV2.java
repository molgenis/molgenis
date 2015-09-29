package org.molgenis.data.rest.v2;

import static java.util.Objects.requireNonNull;

public class ResourcesResponseV2
{
	private final String id;
	private final String location;

	public ResourcesResponseV2(Object id, String locationHref)
	{
		this.id = requireNonNull(id).toString();
		this.location = requireNonNull(locationHref);
	}

	public String getId()
	{
		return id;
	}

	public String getLocation()
	{
		return location;
	}
}

package org.molgenis.data.rest.v2;

import java.util.ArrayList;
import java.util.List;

public class EntityCollectionBatchCreateResponseBodyV2
{
	/**
	 * The location is not added to location response header because it can become too large. The Location will contains
	 * a href to the collection that is created.
	 */
	private String location = "";

	private final List<ResourcesResponseV2> resources = new ArrayList<>();

	public List<ResourcesResponseV2> getResources()
	{
		return resources;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}
}
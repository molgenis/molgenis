package org.molgenis.data.rest.v2;

import java.util.ArrayList;
import java.util.List;

public class EntityCollectionBatchResponseBodyV2
{
	private final List<ResourcesResponseV2> resources = new ArrayList<ResourcesResponseV2>();

	public List<ResourcesResponseV2> getResources()
	{
		return resources;
	}
}
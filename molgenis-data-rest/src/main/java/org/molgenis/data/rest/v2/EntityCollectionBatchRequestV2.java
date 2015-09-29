package org.molgenis.data.rest.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityCollectionBatchRequestV2
{
	private List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();

	public List<Map<String, Object>> getEntities()
	{
		return entities;
	}

	public void setEntities(List<Map<String, Object>> entities)
	{
		this.entities = entities;
	}
}
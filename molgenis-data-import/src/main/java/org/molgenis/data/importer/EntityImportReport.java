package org.molgenis.data.importer;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityImportReport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Integer> nrImportedEntitiesMap;
	private final List<String> newEntities = Lists.newArrayList();

	public EntityImportReport()
	{
		nrImportedEntitiesMap = new HashMap<String, Integer>();
	}

	public void addEntityCount(String entityName, int count)
	{
		Integer entityCount = nrImportedEntitiesMap.get(entityName);
		if (entityCount == null)
		{
			entityCount = 0;
			nrImportedEntitiesMap.put(entityName, entityCount);
		}
		nrImportedEntitiesMap.put(entityName, entityCount + count);
	}

	public Map<String, Integer> getNrImportedEntitiesMap()
	{
		return nrImportedEntitiesMap;
	}

	public void addNewEntity(String entityName)
	{
		newEntities.add(entityName);
	}

	public List<String> getNewEntities()
	{
		return newEntities;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newEntities == null) ? 0 : newEntities.hashCode());
		result = prime * result + ((nrImportedEntitiesMap == null) ? 0 : nrImportedEntitiesMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EntityImportReport other = (EntityImportReport) obj;
		if (newEntities == null)
		{
			if (other.newEntities != null) return false;
		}
		else if (!newEntities.equals(other.newEntities)) return false;
		if (nrImportedEntitiesMap == null)
		{
			if (other.nrImportedEntitiesMap != null) return false;
		}
		else if (!nrImportedEntitiesMap.equals(other.nrImportedEntitiesMap)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		for (String entity : nrImportedEntitiesMap.keySet())
		{
			sb.append("Imported ").append(nrImportedEntitiesMap.get(entity)).append(" ").append(entity)
					.append(" entities.<br />");
		}

		return sb.toString();
	}
}

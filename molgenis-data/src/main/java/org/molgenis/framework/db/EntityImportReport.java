package org.molgenis.framework.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityImportReport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<String> progressLog;
	private Map<String, Integer> nrImportedEntitiesMap;
	private String errorItem;
	private int nrImported;

	public EntityImportReport()
	{
		progressLog = new ArrayList<String>();
		nrImportedEntitiesMap = new HashMap<String,Integer>();
		errorItem = "no error found";
		nrImported = 0;
	}

	public List<String> getProgressLog()
	{
		return progressLog;
	}

	public void setProgressLog(List<String> progressLog)
	{
		this.progressLog = progressLog;
	}

	public void addEntityCount(String entityName, int count)
	{
		Integer entityCount = nrImportedEntitiesMap.get(entityName);
		if (entityCount == null)
		{
			entityCount = 0;
			nrImportedEntitiesMap.put(entityName, entityCount);
		}
        nrImportedEntitiesMap.put(entityName,entityCount + count);
	}

	public Map<String, Integer> getNrImportedEntitiesMap()
	{
		return nrImportedEntitiesMap;
	}

	public void setNrImportedEntitiesMap(Map<String, Integer> nrImportedEntitiesMap)
	{
		this.nrImportedEntitiesMap = nrImportedEntitiesMap;
	}

	public String getErrorItem()
	{
		return errorItem;
	}

	public void setErrorItem(String errorItem)
	{
		this.errorItem = errorItem;
	}

	public int getNrImported()
	{
		return nrImported;
	}

	public void addNrImported(int nrImported)
	{
		this.nrImported += nrImported;
	}

	public void addEntityImportReport(EntityImportReport entityImportReport)
	{
		progressLog.addAll(entityImportReport.getProgressLog());
		for (Map.Entry<String, Integer> entry : entityImportReport.getNrImportedEntitiesMap().entrySet())
		{
			String entityName = entry.getKey();
			Integer entityCount = nrImportedEntitiesMap.get(entityName);
			if (entityCount == null)
			{
				entityCount = 0;
				nrImportedEntitiesMap.put(entityName, entityCount);
			}
            nrImportedEntitiesMap.put(entityName, entry.getValue() + entityCount);
		}
		errorItem = entityImportReport.getErrorItem();
		nrImported += entityImportReport.getNrImported();
	}
}

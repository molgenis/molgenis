package org.molgenis.framework.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityImportReport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private List<String> progressLog;
	private Map<String, AtomicInteger> nrImportedEntitiesMap;
	private String errorItem;
	private int nrImported;

	public EntityImportReport()
	{
		progressLog = new ArrayList<String>();
		nrImportedEntitiesMap = new HashMap<String, AtomicInteger>();
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
		AtomicInteger entityCount = nrImportedEntitiesMap.get(entityName);
		if (entityCount == null)
		{
			entityCount = new AtomicInteger(0);
			nrImportedEntitiesMap.put(entityName, entityCount);
		}
		entityCount.addAndGet(count);
	}

	public Map<String, AtomicInteger> getNrImportedEntitiesMap()
	{
		return nrImportedEntitiesMap;
	}

	public void setNrImportedEntitiesMap(Map<String, AtomicInteger> nrImportedEntitiesMap)
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
		for (Map.Entry<String, AtomicInteger> entry : entityImportReport.getNrImportedEntitiesMap().entrySet())
		{
			String entityName = entry.getKey();
			AtomicInteger entityCount = nrImportedEntitiesMap.get(entityName);
			if (entityCount == null)
			{
				entityCount = new AtomicInteger(0);
				nrImportedEntitiesMap.put(entityName, entityCount);
			}
			entityCount.addAndGet(entry.getValue().get());
		}
		errorItem = entityImportReport.getErrorItem();
		nrImported += entityImportReport.getNrImported();
	}
}

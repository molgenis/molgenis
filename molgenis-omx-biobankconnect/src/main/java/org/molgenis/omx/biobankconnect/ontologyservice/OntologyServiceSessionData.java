package org.molgenis.omx.biobankconnect.ontologyservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.csv.CsvRepository;

public class OntologyServiceSessionData
{
	private final Map<String, String> sessionToOntologyIri;
	private final Map<String, CsvRepository> sessionToCsvRepository;

	public OntologyServiceSessionData()
	{
		sessionToOntologyIri = new HashMap<String, String>();
		sessionToCsvRepository = new HashMap<String, CsvRepository>();
	}

	public void addDataBySession(String sessionId, String ontologyIri, CsvRepository csvRepository)
	{
		sessionToOntologyIri.put(sessionId, ontologyIri);
		sessionToCsvRepository.put(sessionId, csvRepository);
	}

	public CsvRepository getCsvRepositoryBySession(String sessionId)
	{
		return sessionToCsvRepository.containsKey(sessionId) ? sessionToCsvRepository.get(sessionId) : null;
	}

	public String getOntologyIriBySession(String sessionId)
	{
		return sessionToOntologyIri.containsKey(sessionId) ? sessionToOntologyIri.get(sessionId) : null;
	}

	public int getTotalNumberBySession(String sessionId)
	{
		int count = 0;
		try
		{
			if (validationAttributesBySession(sessionId) && sessionToCsvRepository.containsKey(sessionId))
			{
				Iterator<Entity> iterator = sessionToCsvRepository.get(sessionId).iterator();
				while (iterator.hasNext())
				{
					iterator.next();
					count++;
				}
			}
		}
		catch (Exception e)
		{
			return OntologyServiceController.INVALID_TOTAL_NUMBER;
		}
		return count;
	}

	public void removeDataBySession(String sessionId)
	{
		if (sessionToOntologyIri.containsKey(sessionId)) sessionToOntologyIri.remove(sessionId);
		if (sessionToCsvRepository.containsKey(sessionId)) sessionToCsvRepository.remove(sessionId);
	}

	public boolean validationAttributesBySession(String sessionId)
	{
		EntityMetaData entityMetaData = sessionToCsvRepository.get(sessionId).getEntityMetaData();
		for (AttributeMetaData attributeMetaData : entityMetaData.getAttributes())
		{
			if (OntologyService.DEFAULT_MATCHING_NAME_FIELD.equalsIgnoreCase(attributeMetaData.getName())) return true;
		}
		return false;
	}

	public List<Entity> getSubList(String sessionId, int start, int end)
	{
		List<Entity> entities = new ArrayList<Entity>();
		if (sessionToCsvRepository.containsKey(sessionId))
		{
			int count = 0;
			Iterator<Entity> iterator = sessionToCsvRepository.get(sessionId).iterator();
			while (iterator.hasNext())
			{
				Entity next = iterator.next();
				if (count >= start && count <= end)
				{
					entities.add(next);
				}
				count++;
			}
		}
		return entities;
	}
}

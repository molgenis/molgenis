package org.molgenis.ontology.sorta.job;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;

public class SortaJobExecution extends JobExecution
{
	private static final long serialVersionUID = -4666467854597087122L;

	public final static String ENTITY_NAME = "SortaJobExecution";
	public final static String ONTOLOGY_IRI = "ontologyIri";
	public final static String NAME = "name";
	public final static String DELETE_URL = "deleteUrl";
	public final static String SOURCE_ENTITY = "sourceEntity";
	public final static String RESULT_ENTITY = "resultEntity";
	public final static String THRESHOLD = "Threshold";

	private static final String SORTA_MATCH_JOB_TYPE = "SORTA";

	public SortaJobExecution(DataService dataService)
	{
		super(dataService, SortaJobExecutionMetaData.INSTANCE);

		setType(SORTA_MATCH_JOB_TYPE);
	}
	
	public String getName()
	{
		return getString(NAME);
	}
	
	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getResultEntityName()
	{
		return getString(RESULT_ENTITY);
	}
	
	public void setResultEntityName(String resultEntityName)
	{
		set(RESULT_ENTITY, resultEntityName);
	}
	
	public String getSourceEntityName()
	{
		return getString(SOURCE_ENTITY);
	}
	
	public void setSourceEntityName(String sourceEntityName)
	{
		set(SOURCE_ENTITY, sourceEntityName);
	}

	public String getDeleteUrl()
	{
		return getString(DELETE_URL);
	}

	public void setDeleteUrl(String deleteUrl)
	{
		set(DELETE_URL, deleteUrl);
	}

	public void setOntologyIri(String ontologyIri)
	{
		set(ONTOLOGY_IRI, ontologyIri);
	}

	public String getOntologyIri()
	{
		return getString(ONTOLOGY_IRI);
	}
	
	public void setThreshold(double threshold)
	{
		set(THRESHOLD, threshold);
	}
	
	public double getThreshold()
	{
		return getDouble(THRESHOLD).doubleValue();
	}
}

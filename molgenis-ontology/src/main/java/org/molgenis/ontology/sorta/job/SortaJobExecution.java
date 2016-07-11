package org.molgenis.ontology.sorta.job;

import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.DELETE_URL;
import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.NAME;
import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.ONTOLOGY_IRI;
import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.RESULT_ENTITY;
import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.SORTA_MATCH_JOB_TYPE;
import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.SOURCE_ENTITY;
import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.THRESHOLD;

import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityMetaData;

public class SortaJobExecution extends JobExecution
{
	public SortaJobExecution(Entity entity)
	{
		super(entity);
	}

	public SortaJobExecution(EntityMetaData entityMeta)
	{
		super(entityMeta);
		setDefaultValues();
	}

	public SortaJobExecution(String identifier, EntityMetaData entityMeta)
	{
		super(identifier, entityMeta);
		setDefaultValues();
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

	private void setDefaultValues()
	{
		setType(SORTA_MATCH_JOB_TYPE);
	}
}

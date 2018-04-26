package org.molgenis.ontology.sorta.job;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData.*;

public class SortaJobExecution extends JobExecution
{
	public SortaJobExecution(Entity entity)
	{
		super(entity);
	}

	public SortaJobExecution(EntityType entityType)
	{
		super(entityType);
		setDefaultValues();
	}

	public SortaJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
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
		return getDouble(THRESHOLD);
	}

	private void setDefaultValues()
	{
		setType(SORTA_MATCH_JOB_TYPE);
	}
}

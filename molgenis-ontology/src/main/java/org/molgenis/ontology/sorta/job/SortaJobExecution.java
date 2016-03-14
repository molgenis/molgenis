package org.molgenis.ontology.sorta.job;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecution;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetaData;

public class SortaJobExecution extends JobExecution
{
	private static final long serialVersionUID = -4666467854597087122L;

	public final static String ENTITY_NAME = "SortaJobExecution";
	public final static String ONTOLOGY_IRI = "ontologyIri";
	public final static String DELETE_URL = "deleteUrl";
	public final static String TARGET_ENTITY = "targetEntity";

	public SortaJobExecution(DataService dataService)
	{
		super(dataService, SortaJobExecutionMetaData.INSTANCE);
	}

	public void setTargetEntityName(String targetEntityName)
	{
		set(TARGET_ENTITY, targetEntityName);
	}

	public String getDeleteUrl()
	{
		return getString(DELETE_URL);
	}

	public void setDeleteUrl(String deleteUrl)
	{
		set(DELETE_URL, deleteUrl);
	}

	public String getTargetEntityName()
	{
		return getString(TARGET_ENTITY);
	}

	public void setOntologyIri(String ontologyIri)
	{
		set(ONTOLOGY_IRI, ontologyIri);
	}

	public String getOntologyIri()
	{
		return getString(ONTOLOGY_IRI);
	}
}

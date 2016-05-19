package org.molgenis.ontology.sorta.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.DELETE_URL;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.ENTITY_NAME;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.NAME;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.ONTOLOGY_IRI;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.RESULT_ENTITY;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.SOURCE_ENTITY;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.THRESHOLD;

import org.molgenis.auth.AuthorityMetaData;
import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SortaJobExecutionMetaData extends SystemEntityMetaDataImpl
{
	private JobExecutionMetaData jobExecutionMetaData;

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		setExtends(jobExecutionMetaData);
		addAttribute(NAME).setDataType(STRING).setNillable(false);
		addAttribute(RESULT_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(SOURCE_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(ONTOLOGY_IRI).setDataType(STRING).setNillable(false);
		addAttribute(DELETE_URL).setDataType(HYPERLINK).setNillable(false);
		addAttribute(THRESHOLD).setDataType(DECIMAL).setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setJobExecutionMetaData(JobExecutionMetaData jobExecutionMetaData)
	{
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
	}
}
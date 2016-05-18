package org.molgenis.ontology.sorta.meta;

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

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.springframework.stereotype.Component;

@Component
public class SortaJobExecutionMetaData extends SystemEntityMetaDataImpl
{
	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		setExtends(new JobExecutionMetaData());
		addAttribute(NAME).setDataType(STRING).setNillable(false);
		addAttribute(RESULT_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(SOURCE_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(ONTOLOGY_IRI).setDataType(STRING).setNillable(false);
		addAttribute(DELETE_URL).setDataType(HYPERLINK).setNillable(false);
		addAttribute(THRESHOLD).setDataType(DECIMAL).setNillable(false);
	}
}
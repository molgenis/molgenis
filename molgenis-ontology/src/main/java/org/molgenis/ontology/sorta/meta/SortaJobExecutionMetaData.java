package org.molgenis.ontology.sorta.meta;

import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.DELETE_URL;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.ENTITY_NAME;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.ONTOLOGY_IRI;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.RESULT_ENTITY;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.SOURCE_ENTITY;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.THRESHOLD;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.NAME;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.springframework.stereotype.Component;

@Component
public class SortaJobExecutionMetaData extends DefaultEntityMetaData
{
	public final static SortaJobExecutionMetaData INSTANCE = new SortaJobExecutionMetaData();

	public SortaJobExecutionMetaData()
	{
		super(ENTITY_NAME, SortaJobExecution.class);
		setExtends(new JobExecutionMetaData());
		addAttribute(NAME).setDataType(STRING).setNillable(false);
		addAttribute(RESULT_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(SOURCE_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(ONTOLOGY_IRI).setDataType(STRING).setNillable(false);
		addAttribute(DELETE_URL).setDataType(HYPERLINK).setNillable(false);
		addAttribute(THRESHOLD).setDataType(DECIMAL).setNillable(false);
	}
}
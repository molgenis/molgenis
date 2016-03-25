package org.molgenis.ontology.sorta.meta;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.DELETE_URL;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.ENTITY_NAME;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.ONTOLOGY_IRI;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.RESULT_ENTITY;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.SOURCE_ENTITY;
import static org.molgenis.ontology.sorta.job.SortaJobExecution.THRESHOLD;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
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
		addAttribute(RESULT_ENTITY).setDataType(STRING).setNillable(false);
		addAttribute(SOURCE_ENTITY).setDataType(STRING).setNillable(false);
		// TODO: URL
		addAttribute(ONTOLOGY_IRI).setDataType(STRING).setNillable(false);
		// TODO: URL
		addAttribute(DELETE_URL).setDataType(STRING).setNillable(false);
		addAttributeMetaData(new DefaultAttributeMetaData(THRESHOLD, DECIMAL).setNillable(false));
	}
}
package org.molgenis.ontology.sorta.meta;

import org.molgenis.MolgenisFieldTypes;
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
		super(SortaJobExecution.ENTITY_NAME, SortaJobExecution.class);
		setExtends(new JobExecutionMetaData());
		addAttribute(SortaJobExecution.TARGET_ENTITY).setDataType(MolgenisFieldTypes.STRING).setNillable(false);
		addAttribute(SortaJobExecution.ONTOLOGY_IRI).setDataType(MolgenisFieldTypes.STRING).setNillable(false);
		addAttribute(SortaJobExecution.DELETE_URL).setDataType(MolgenisFieldTypes.STRING).setNillable(false);
	}
}
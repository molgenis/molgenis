package org.molgenis.gavin.job;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.STRING;

@Component
public class GavinJobExecutionMetaData extends DefaultEntityMetaData
{
	public static final String GAVIN_JOB_EXECUTION = "GavinJobExecution";
	public static final String FILENAME = "filename";

	public GavinJobExecutionMetaData()
	{
		super(GAVIN_JOB_EXECUTION, GavinJobExecution.class);
		setExtends(new JobExecutionMetaData());
		addAttributeMetaData(new DefaultAttributeMetaData(FILENAME, STRING).setNillable(false));
	}
}

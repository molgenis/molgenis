package org.molgenis.gavin.job;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class GavinJobExecutionMetaData extends DefaultEntityMetaData
{
	public static final String GAVIN_JOB_EXECUTION = "GavinJobExecution";
	public GavinJobExecutionMetaData()
	{
		super(GAVIN_JOB_EXECUTION, GavinJobExecution.class);
		setExtends(new JobExecutionMetaData());
	}
}

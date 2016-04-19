package org.molgenis.gavin.job;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class GavinJobExecutionMetaData extends DefaultEntityMetaData
{
	public GavinJobExecutionMetaData()
	{
		super(GavinJobExecution.ENTITY_NAME, GavinJobExecution.class);
		setExtends(new JobExecutionMetaData());
	}
}

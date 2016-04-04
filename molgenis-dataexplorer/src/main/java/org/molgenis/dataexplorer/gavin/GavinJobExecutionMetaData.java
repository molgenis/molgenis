package org.molgenis.dataexplorer.gavin;

import org.molgenis.data.jobs.JobExecutionMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class GavinJobExecutionMetaData extends DefaultEntityMetaData
{
	private static final String ENTITY_NAME = "GavinJobExecution";

	public GavinJobExecutionMetaData()
	{
		super(ENTITY_NAME, GavinJobExecution.class);
		setExtends(new JobExecutionMetaData());
	}
}

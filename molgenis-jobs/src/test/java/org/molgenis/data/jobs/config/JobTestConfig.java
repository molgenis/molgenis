package org.molgenis.data.jobs.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.jobs.JobExecutionUpdater;
import org.molgenis.data.jobs.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@Configuration
@Import({ EntityBaseTestConfig.class, JobExecutionMetaData.class, ScheduledJobMetadata.class, ScheduledJobTypeMetadata.class,
		ScheduledJobTypeFactory.class, ScheduledJobFactory.class })
public class JobTestConfig
{
	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return mock(JobExecutionUpdater.class);
	}

}

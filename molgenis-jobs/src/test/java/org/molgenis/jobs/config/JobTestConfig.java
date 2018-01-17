package org.molgenis.jobs.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.jobs.JobExecutionUpdater;
import org.molgenis.jobs.model.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@Configuration
@Import({ EntityBaseTestConfig.class, JobExecutionMetaData.class, ScheduledJobMetadata.class,
		ScheduledJobTypeMetadata.class, ScheduledJobTypeFactory.class, ScheduledJobFactory.class, JobPackage.class })
public class JobTestConfig
{
	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return mock(JobExecutionUpdater.class);
	}

}

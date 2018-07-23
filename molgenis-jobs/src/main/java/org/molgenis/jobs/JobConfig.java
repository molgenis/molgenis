package org.molgenis.jobs;

import org.molgenis.jobs.scheduler.SchedulerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Jobs configuration
 */
@Configuration
@Import(SchedulerConfig.class)
public class JobConfig
{
	@Autowired
	private JobExecutorTokenService jobExecutorTokenService;

	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return new JobExecutionUpdaterImpl(jobExecutorTokenService);
	}
}

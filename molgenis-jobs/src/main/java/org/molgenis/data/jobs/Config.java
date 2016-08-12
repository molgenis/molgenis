package org.molgenis.data.jobs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jobs configuration
 */
@Configuration
public class Config
{
	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return new JobExecutionUpdaterImpl();
	}
}

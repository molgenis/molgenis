package org.molgenis.data.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Jobs configuration
 */
@Configuration
public class JobConfig
{
	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Bean
	public JobExecutionTemplate jobExecutionTemplate()
	{
		return new JobExecutionTemplate(new TransactionTemplate(platformTransactionManager));
	}

	@Bean
	public JobExecutionUpdater jobExecutionUpdater()
	{
		return new JobExecutionUpdaterImpl();
	}

}

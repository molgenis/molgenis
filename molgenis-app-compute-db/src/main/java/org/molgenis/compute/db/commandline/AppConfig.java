package org.molgenis.compute.db.commandline;

import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * String config form the commandline apps
 * 
 * @author erwin
 * 
 */
@Configuration
public class AppConfig
{
	@Scope("prototype")
	@Bean
	public Database unathorizedDatabase() throws DatabaseException
	{
		return new app.JpaDatabase();
	}

	@Bean
	public ApplicationContextProvider applicationContextProvider()
	{
		return new ApplicationContextProvider();
	}

	@Bean
	public Scheduler scheduler()
	{
		return new Scheduler(taskScheduler());
	}

	@Bean(destroyMethod = "shutdown")
	public TaskScheduler taskScheduler()
	{
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(2);

		return scheduler;
	}

}

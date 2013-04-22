package org.molgenis.compute.db.commandline;

import org.molgenis.compute.db.executor.ComputeExecutor;
import org.molgenis.compute.db.executor.ComputeExecutorPilotDB;
import org.molgenis.compute.db.executor.ComputeExecutorTask;
import org.molgenis.compute.db.importer.WorkflowImporter;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	@Bean(destroyMethod = "close")
	public Database unathorizedDatabase() throws DatabaseException
	{
		return new app.JpaDatabase();
	}

	@Bean
	public ComputeExecutorTask computeExecutorTask() throws DatabaseException
	{
		return new ComputeExecutorTask(computeExecutor(), taskScheduler());
	}

	@Bean
	public ComputeExecutor computeExecutor() throws DatabaseException
	{
		return new ComputeExecutorPilotDB(unathorizedDatabase());
	}

	@Bean
	public WorkflowImporter workflowImporter() throws DatabaseException
	{
		return new WorkflowImporter(unathorizedDatabase());
	}

	@Bean(destroyMethod = "shutdown")
	public TaskScheduler taskScheduler()
	{
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(2);

		return scheduler;
	}

}

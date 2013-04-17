package org.molgenis.compute.db.commandline;

import org.molgenis.compute.db.executor.ComputeExecutor;
import org.molgenis.compute.db.executor.ComputeExecutorPilotDB;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@Configuration
@ComponentScan("org.molgenis.compute.db.commandline")
public class AppConfig
{

	@Bean(destroyMethod = "close")
	public Database unathorizedDatabase() throws DatabaseException
	{
		return new app.JpaDatabase();
	}

	@Bean
	public ComputeExecutor computeExecutor() throws DatabaseException
	{
		return new ComputeExecutorPilotDB(unathorizedDatabase());
	}

}

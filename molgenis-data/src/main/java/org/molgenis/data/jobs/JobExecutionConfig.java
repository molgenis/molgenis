package org.molgenis.data.jobs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobExecutionConfig
{
	@Bean
	public ExecutorService executorService()
	{
		return Executors.newWorkStealingPool();
	}
}

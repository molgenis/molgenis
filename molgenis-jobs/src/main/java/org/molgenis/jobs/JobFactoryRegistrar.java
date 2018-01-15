package org.molgenis.jobs;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Discovers and registers {@link JobFactory} beans with the {@link JobFactoryRegistry}
 */
@Component
public class JobFactoryRegistrar
{
	private final JobFactoryRegistry jobFactoryRegistry;

	public JobFactoryRegistrar(JobFactoryRegistry jobFactoryRegistry)
	{
		this.jobFactoryRegistry = requireNonNull(jobFactoryRegistry);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, JobFactory> jobFactoryMap = ctx.getBeansOfType(JobFactory.class);
		jobFactoryMap.values().forEach(this::register);
	}

	private void register(JobFactory jobFactory)
	{
		jobFactoryRegistry.registerJobFactory(jobFactory);
	}
}

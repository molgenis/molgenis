package org.molgenis.data.jobs;

import org.molgenis.data.jobs.model.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

/**
 * Gets the JobFactory for a JobExecution.
 */
@Component
public class JobFactoryRegistry
{
	private Map<Class<?>, JobFactory> jobFactories;

	@Autowired
	public JobFactoryRegistry(List<JobFactory> jobFactories)
	{
		this.jobFactories = jobFactories.stream()
				.collect(toMap(factory -> resolveTypeArgument(factory.getClass(), JobFactory.class), identity()));
	}

	public JobFactory getJobFactory(JobExecution jobExecution)
	{
		return jobFactories.get(jobExecution.getClass());
	}
}

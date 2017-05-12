package org.molgenis.data.jobs;

import org.molgenis.data.jobs.model.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Gets the JobFactory for a JobExecution. The pairs are matched by trial and error and then cached.
 * This negates the need to expose an identifier in the JobFactory beans. Ideally we'd get the generic type of
 * JobFactory at runtime but this is not possible when using lambdas (see http://stackoverflow.com/a/25612775).
 */
@Component
public class JobFactoryRegistry
{
	private List<JobFactory> jobFactories;

	@Autowired
	public JobFactoryRegistry(List<JobFactory> jobFactories)
	{
		this.jobFactories = requireNonNull(jobFactories);
	}

	public JobFactory getJobFactory(JobExecution jobExecution)
	{
		for (JobFactory jobFactory : jobFactories)
		{
			Class<?> clazz = GenericTypeResolver.resolveTypeArgument(jobFactory.getClass(), JobFactory.class);
			if (clazz.getName().equals(jobExecution.getClass().getName()))
			{
				return jobFactory;
			}
		}
		return null;
	}
}

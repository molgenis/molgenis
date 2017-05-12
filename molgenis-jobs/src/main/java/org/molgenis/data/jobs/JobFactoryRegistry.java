package org.molgenis.data.jobs;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.jobs.model.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
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
	private Map<Class, JobFactory> cache;

	@Autowired
	public JobFactoryRegistry(List<JobFactory> jobFactories)
	{
		this.jobFactories = requireNonNull(jobFactories);
		this.cache = newHashMap();
	}

	public JobFactory getJobFactory(JobExecution jobExecution)
	{
		return cache.getOrDefault(jobExecution.getClass(), tryJobFactories(jobExecution));
	}

	@SuppressWarnings("unchecked")
	private JobFactory tryJobFactories(JobExecution jobExecution)
	{
		for (JobFactory jobFactory : jobFactories)
		{
			try
			{
				jobFactory.createJob(jobExecution);
				cache.putIfAbsent(jobExecution.getClass(), jobFactory);
				return jobFactory;
			}
			catch (Exception ex)
			{
				// ignore mismatching JobFactories and JobExecutions
			}
		}

		throw new MolgenisDataException(
				String.format("No factory found for JobExecution class [%s]", jobExecution.getClass().getName()));
	}
}

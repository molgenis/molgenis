package org.molgenis.jobs.schedule;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.validation.JsonValidator;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.jobs.model.ScheduledJobMetadata;
import org.molgenis.security.core.utils.SecurityUtils;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Decorator that makes sure all active {@link ScheduledJob} instances are scheduled using the {@link JobScheduler}.
 */
public class ScheduledJobRepositoryDecorator extends AbstractRepositoryDecorator<ScheduledJob>
{
	private final JobScheduler scheduler;
	private final JsonValidator jsonValidator;

	ScheduledJobRepositoryDecorator(Repository<ScheduledJob> delegateRepository, JobScheduler scheduler,
			JsonValidator jsonValidator)
	{
		super(delegateRepository);
		this.scheduler = requireNonNull(scheduler);
		this.jsonValidator = jsonValidator;
	}

	@Override
	public void update(ScheduledJob scheduledJob)
	{
		validateJobParameters(scheduledJob);
		setUsername(scheduledJob);
		delegate().update(scheduledJob);
		scheduler.schedule(scheduledJob);
	}

	@Override
	public void update(Stream<ScheduledJob> scheduledJobs)
	{
		delegate().update(scheduledJobs.filter(job ->
		{
			validateJobParameters(job);
			setUsername(job);
			scheduler.schedule(job);
			return true;
		}));
	}

	@Override
	public void delete(ScheduledJob scheduledJob)
	{
		String entityId = scheduledJob.getId();
		delegate().delete(scheduledJob);
		scheduler.unschedule(entityId);
	}

	@Override
	public void delete(Stream<ScheduledJob> jobs)
	{
		delegate().delete(jobs.filter(job ->
		{
			String entityId = job.getId();
			scheduler.unschedule(entityId);
			return true;
		}));
	}

	@Override
	public void deleteById(Object id)
	{
		if (id instanceof String)
		{
			String entityId = (String) id;
			scheduler.unschedule(entityId);
		}
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delegate().deleteAll(ids.filter(id ->
		{
			if (id instanceof String)
			{
				String entityId = (String) id;
				scheduler.unschedule(entityId);
			}
			return true;
		}));
	}

	@Override
	public void deleteAll()
	{
		for (Entity e : this)
		{
			String entityId = e.getString(ScheduledJobMetadata.ID);
			scheduler.unschedule(entityId);
		}
		delegate().deleteAll();
	}

	@Override
	public void add(ScheduledJob scheduledJob)
	{
		validateJobParameters(scheduledJob);
		setUsername(scheduledJob);
		delegate().add(scheduledJob);
		scheduler.schedule(scheduledJob);
	}

	@Override
	public Integer add(Stream<ScheduledJob> jobs)
	{
		return delegate().add(jobs.filter(job ->
		{
			validateJobParameters(job);
			setUsername(job);
			scheduler.schedule(job);
			return true;
		}));
	}

	private void validateJobParameters(ScheduledJob scheduledJob)
	{
		jsonValidator.validate(scheduledJob.getParameters(), scheduledJob.getType().getSchema());
	}

	private static void setUsername(ScheduledJob job)
	{
		job.setUser(SecurityUtils.getCurrentUsername());
	}
}
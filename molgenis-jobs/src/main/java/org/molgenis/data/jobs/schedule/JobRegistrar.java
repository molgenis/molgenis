package org.molgenis.data.jobs.schedule;

import org.molgenis.data.DataService;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

/**
 * Discovers {@link ScheduledJob}s and schedules them using {@link JobScheduler}
 */
@Component
public class JobRegistrar
{
	private final JobScheduler jobScheduler;
	private final DataService dataService;

	@Autowired
	public JobRegistrar(JobScheduler jobScheduler, DataService dataService)
	{
		this.jobScheduler = requireNonNull(jobScheduler);
		this.dataService = requireNonNull(dataService);
	}

	public void scheduleJobs()
	{
		dataService.findAll(SCHEDULED_JOB, ScheduledJob.class).forEach(jobScheduler::schedule);
	}
}

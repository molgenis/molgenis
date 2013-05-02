package org.molgenis.compute.db.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.runtime.ComputeRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

/**
 * Schedule and unschedule pilot jobs
 * 
 * @author erwin
 * 
 */
public class Scheduler
{
	private final TaskScheduler taskScheduler;
	private final Map<Integer, ScheduledFuture<?>> scheduledJobs = new HashMap<Integer, ScheduledFuture<?>>();

	@Autowired
	public Scheduler(TaskScheduler taskScheduler)
	{
		this.taskScheduler = taskScheduler;
	}

	public synchronized void schedule(ComputeRun run, String username, String password)
	{
		if (scheduledJobs.containsKey(run.getId()))
		{
			throw new ComputeDbException("Run " + run.getName() + " already running");
		}

		ComputeJob job = new ComputeJob(new ComputeExecutorPilotDB(), run, username, password);
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(job, run.getPollDelay());
		scheduledJobs.put(run.getId(), future);
	}

	public synchronized boolean isRunning(Integer computeRunId)
	{
		return scheduledJobs.containsKey(computeRunId);
	}

	public synchronized void unschedule(Integer computeRunId)
	{
		if (!isRunning(computeRunId))
		{
			throw new ComputeDbException("Not running");
		}

		ScheduledFuture<?> future = scheduledJobs.get(computeRunId);
		future.cancel(false);
		scheduledJobs.remove(computeRunId);
	}
}

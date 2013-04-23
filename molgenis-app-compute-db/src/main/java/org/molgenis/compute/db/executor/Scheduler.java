package org.molgenis.compute.db.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.runtime.ComputeHost;
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

	public synchronized void schedule(ComputeHost host, String password)
	{
		if (scheduledJobs.containsKey(host.getId()))
		{
			throw new ComputeDbException("Host " + host.getName() + " already running");
		}

		ComputeJob job = new ComputeJob(new ComputeExecutorPilotDB(), host, password);
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(job, host.getPollDelay());
		scheduledJobs.put(host.getId(), future);
	}

	public synchronized boolean isRunning(Integer computeHostId)
	{
		return scheduledJobs.containsKey(computeHostId);
	}

	public synchronized void unschedule(Integer computeHostId)
	{
		if (!isRunning(computeHostId))
		{
			throw new ComputeDbException("Not running");
		}

		ScheduledFuture<?> future = scheduledJobs.get(computeHostId);
		future.cancel(false);
		scheduledJobs.remove(computeHostId);
	}
}

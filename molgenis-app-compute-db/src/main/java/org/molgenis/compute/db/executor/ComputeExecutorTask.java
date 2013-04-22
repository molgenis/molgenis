package org.molgenis.compute.db.executor;

import java.util.concurrent.ScheduledFuture;

import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.runtime.ComputeHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

/**
 * Runnable that calls executeTasks on a ComputeExecutor.
 * 
 * You can start and stop the task
 * 
 * @author erwin
 * 
 */
public class ComputeExecutorTask implements Runnable
{
	private final ComputeExecutor executor;
	private final TaskScheduler taskScheduler;
	private ScheduledFuture<?> scheduledFuture;
	private ComputeHost computeHost;
	private String password;

	public ComputeHost getComputeHost()
	{
		return computeHost;
	}

	@Autowired
	public ComputeExecutorTask(ComputeExecutor executor, TaskScheduler taskScheduler)
	{
		if (executor == null) throw new IllegalArgumentException("ComputeExecutor is null");
		if (taskScheduler == null) throw new IllegalArgumentException("TaskScheduler is nul");

		this.executor = executor;
		this.taskScheduler = taskScheduler;
	}

	public boolean isRunning()
	{
		return (scheduledFuture != null) && !scheduledFuture.isDone();
	}

	public void start(ComputeHost computeHost)
	{
		start(computeHost, null);
	}

	public void start(ComputeHost computeHost, String password)
	{
		if (isRunning())
		{
			throw new ComputeDbException("Task already started");
		}

		this.computeHost = computeHost;
		this.password = password;

		scheduledFuture = taskScheduler.scheduleWithFixedDelay(this, computeHost.getPollDelay());
	}

	public void stop()
	{
		if (!isRunning())
		{
			throw new ComputeDbException("Task not running");
		}

		scheduledFuture.cancel(true);
	}

	@Override
	public void run()
	{
		executor.executeTasks(computeHost, password);
	}

}

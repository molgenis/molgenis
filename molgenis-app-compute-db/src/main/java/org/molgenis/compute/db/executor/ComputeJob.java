package org.molgenis.compute.db.executor;

import org.molgenis.compute.runtime.ComputeRun;

public class ComputeJob implements Runnable
{
	private final ComputeExecutor executor;
	private final PilotManager pilotManager = new PilotManager();
	private final ComputeRun computeRun;
	private final String username;
	private final String password;

	public ComputeJob(ComputeExecutor executor, ComputeRun computeRun, String username, String password)
	{
		this.executor = executor;
		this.computeRun = computeRun;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run()
	{
		computeRun.setIsActive(true);
		computeRun.setIsSubmittingPilots(true);
		executor.executeTasks(computeRun, username, password);
	}

}

package org.molgenis.compute.db.executor;

import org.molgenis.compute.runtime.ComputeHost;

public class ComputeJob implements Runnable
{
	private final ComputeExecutor executor;
	private final ComputeHost computeHost;
	private final String password;

	public ComputeJob(ComputeExecutor executor, ComputeHost computeHost, String password)
	{
		this.executor = executor;
		this.computeHost = computeHost;
		this.password = password;
	}

	@Override
	public void run()
	{
		executor.executeTasks(computeHost, password);
	}

}

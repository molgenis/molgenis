package org.molgenis.compute.db.controller;

import org.molgenis.compute.db.executor.ComputeExecutorTask;

public class PilotForm
{
	private Integer computeHostId;
	private String password;
	private boolean running;

	public PilotForm()
	{
	}

	public PilotForm(ComputeExecutorTask computeExecutorTask)
	{
		if (computeExecutorTask.getComputeHost() != null)
		{
			computeHostId = computeExecutorTask.getComputeHost().getId();
		}

		this.running = computeExecutorTask.isRunning();
	}

	public Integer getComputeHostId()
	{
		return computeHostId;
	}

	public void setComputeHostId(Integer computeHostId)
	{
		this.computeHostId = computeHostId;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean isRunning()
	{
		return running;
	}

	public void setRunning(boolean running)
	{
		this.running = running;
	}

}
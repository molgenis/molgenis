package org.molgenis.compute.db.controller;

import java.util.Date;

public class RunModel
{
	private final String name;
	private final boolean running;
	private final String backendUrl;
	private final Date creationTime;

	public RunModel(String name, boolean running, String backendUrl, Date creationTime)
	{
		this.name = name;
		this.running = running;
		this.backendUrl = backendUrl;
		this.creationTime = creationTime;
	}

	public String getName()
	{
		return name;
	}

	public boolean isRunning()
	{
		return running;
	}

	public String getBackendUrl()
	{
		return backendUrl;
	}

	public Date getCreationTime()
	{
		return creationTime;
	}

}
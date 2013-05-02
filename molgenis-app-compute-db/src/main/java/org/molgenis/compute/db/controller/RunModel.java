package org.molgenis.compute.db.controller;

public class RunModel
{
	private final String name;
	private final boolean running;
	private final String backendUrl;

	public RunModel(String name, boolean running, String backendUrl)
	{
		this.name = name;
		this.running = running;
		this.backendUrl = backendUrl;
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

}
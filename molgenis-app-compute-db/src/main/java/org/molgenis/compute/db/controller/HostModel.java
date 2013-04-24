package org.molgenis.compute.db.controller;

public class HostModel
{
	private final Integer id;
	private final String name;
	private final boolean running;

	public HostModel(Integer id, String name, boolean running)
	{
		this.id = id;
		this.name = name;

		this.running = running;
	}

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public boolean isRunning()
	{
		return running;
	}

}
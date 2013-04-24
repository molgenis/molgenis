package org.molgenis.compute.db.controller;

public class TaskStatusModel
{
	private final int generated;
	private final int ready;
	private final int running;
	private final int done;
	private final int failed;

	public TaskStatusModel(int generated, int ready, int running, int done, int failed)
	{
		this.generated = generated;
		this.ready = ready;
		this.running = running;
		this.done = done;
		this.failed = failed;
	}

	public int getGenerated()
	{
		return generated;
	}

	public int getReady()
	{
		return ready;
	}

	public int getRunning()
	{
		return running;
	}

	public int getDone()
	{
		return done;
	}

	public int getFailed()
	{
		return failed;
	}

}

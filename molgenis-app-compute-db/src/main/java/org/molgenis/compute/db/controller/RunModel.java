package org.molgenis.compute.db.controller;

import java.util.Date;

public class RunModel
{
	private final String name;
	private final boolean running;
    private final boolean submitting;
    private final boolean complete;
	private final boolean owned;
	private final String backendUrl;
	private final Date creationTime;
	private final String owner;

    public RunModel(String name, boolean running, boolean submitting, boolean complete, boolean owned,
					String backendUrl, Date creationTime, String owner)
	{
		this.name = name;
		this.running = running;
        this.submitting = submitting;
        this.complete = complete;
		this.owned = owned;
		this.backendUrl = backendUrl;
		this.creationTime = creationTime;
		this.owner = owner;
	}

	public String getName()
	{
		return name;
	}

	public boolean isRunning()
	{
		return running;
	}

    public boolean isSubmitting()
    {
        return submitting;
    }

    public boolean isComplete()
    {
        return complete;
    }

	public boolean isOwned()
	{
		return owned;
	}

	public String getBackendUrl()
	{
		return backendUrl;
	}

	public Date getCreationTime()
	{
		return creationTime;
	}

	public String getOwner()
	{
		return owner;
	}
}
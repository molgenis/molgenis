package org.molgenis.data.event;

public class BootstrappingEvent
{
	private boolean done;

	public BootstrappingEvent(boolean done)
	{
		this.done = done;
	}

	public boolean isDone()
	{
		return this.done;
	}
}
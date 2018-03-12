package org.molgenis.data.event;

public class BootstrappingEvent
{
	public enum BootstrappingStatus
	{
		FINISHED_SYSTEM_ENTITY_TYPES;
	}

	private final BootstrappingStatus status;

	public BootstrappingEvent(BootstrappingStatus status)
	{
		this.status = status;
	}

	public BootstrappingStatus getStatus()
	{
		return this.status;
	}
}
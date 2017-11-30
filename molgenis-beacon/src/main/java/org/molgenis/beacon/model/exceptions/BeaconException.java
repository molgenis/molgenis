package org.molgenis.beacon.model.exceptions;

/**
 * Beacon exception that can be handled by the {@link BeaconExceptionHandler}
 */
public abstract class BeaconException extends RuntimeException
{
	public BeaconException()
	{
	}

	public BeaconException(String message)
	{
		super(message);
	}
}

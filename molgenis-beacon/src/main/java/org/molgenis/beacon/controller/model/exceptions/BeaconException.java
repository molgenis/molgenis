package org.molgenis.beacon.controller.model.exceptions;

/**
 * Beacon exception that can be handled by the {@link BeaconExceptionHandler}
 *
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
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

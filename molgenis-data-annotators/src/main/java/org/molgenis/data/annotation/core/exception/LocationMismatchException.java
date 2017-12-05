package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.annotation.core.datastructures.Location;

import static java.util.Objects.requireNonNull;

public class LocationMismatchException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN05";
	private final transient Location location;
	private final transient Location thisLoc;

	public LocationMismatchException(Location location, Location thisLoc)
	{
		super(ERROR_CODE);
		this.location = requireNonNull(location);
		this.thisLoc = requireNonNull(thisLoc);

	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s id:%s", location, thisLoc);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { location, thisLoc };
	}
}

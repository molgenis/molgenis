package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.annotation.core.datastructures.Location;

public class LocationMismatchException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN05";
	private int sourceEntitiesSize;
	private Location location;
	private Location thisLoc;

	public LocationMismatchException(Location location, Location thisLoc)
	{
		super(ERROR_CODE);
		this.location = location;
		this.thisLoc = thisLoc;
	}
}

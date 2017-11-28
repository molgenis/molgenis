package org.molgenis.data.rest.exception;

import static java.util.Objects.requireNonNull;

/**
 * Exception to be thrown if a vlue expected in the request was missing
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MissingValueException extends RestApiException
{
	private static final String ERROR_CODE = "R05";
	private final String missingValue;
	private final String location;

	public MissingValueException(String missingValue, String location)
	{
		super(ERROR_CODE);
		this.missingValue = requireNonNull(missingValue);
		this.location = requireNonNull(location);
	}

	public String getMissingValue()
	{
		return missingValue;
	}

	public String getLocation()
	{
		return location;
	}

	@Override
	public String getMessage()
	{
		return String.format("missing:%s location:%s", missingValue, location);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { missingValue, location };
	}
}

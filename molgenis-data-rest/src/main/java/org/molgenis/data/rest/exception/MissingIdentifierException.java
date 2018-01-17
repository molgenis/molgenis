package org.molgenis.data.rest.exception;

import static java.util.Objects.requireNonNull;

/**
 * Exception thrown when an operation on a entity was attempted without specifying the identifier for the entity
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MissingIdentifierException extends RestApiException
{
	private static final String ERROR_CODE = "R09";
	private final int count;

	public MissingIdentifierException(int count)
	{
		super(ERROR_CODE);
		this.count = requireNonNull(count);
	}

	@Override
	public String getMessage()
	{
		return String.format("index:%s", count);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { count };
	}
}
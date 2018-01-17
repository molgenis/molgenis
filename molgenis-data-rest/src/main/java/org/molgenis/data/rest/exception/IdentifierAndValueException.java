package org.molgenis.data.rest.exception;

/**
 * thrown where we do expect a key value pair (an identifier and a value) but got more than 2 arguments
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IdentifierAndValueException extends RestApiException
{
	private static final String ERROR_CODE = "R10";

	public IdentifierAndValueException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "";
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}
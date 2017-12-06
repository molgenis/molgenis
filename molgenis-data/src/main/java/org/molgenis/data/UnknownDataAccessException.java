package org.molgenis.data;

/**
 * TODO discuss: extend from UncategorizedDataAccessException?
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownDataAccessException extends ErrorCodedDataAccessException
{
	private static final String ERROR_CODE = "D99";

	public UnknownDataAccessException(Throwable cause)
	{
		super(ERROR_CODE, cause);
	}

	@Override
	public String getMessage()
	{
		return "unknown error";
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}

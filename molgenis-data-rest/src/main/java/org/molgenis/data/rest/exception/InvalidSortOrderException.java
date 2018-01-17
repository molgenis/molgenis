package org.molgenis.data.rest.exception;

/**
 * thrown if SORT order is specified but not "ASC" or "DESC"
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidSortOrderException extends RestApiException
{
	private static final String ERROR_CODE = "R06";

	public InvalidSortOrderException()
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

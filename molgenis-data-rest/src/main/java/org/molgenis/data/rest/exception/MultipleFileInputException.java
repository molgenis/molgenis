package org.molgenis.data.rest.exception;

/**
 * Exception to be thrown if an entity is created via a form and more than one file was submitted via this form
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MultipleFileInputException extends RestApiException
{
	private static final String ERROR_CODE = "R07";

	public MultipleFileInputException()
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

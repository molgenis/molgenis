package org.molgenis.data.rest.exception;

/**
 * thrown if an update was attempeted on an entity with a file attribute, but without sending the value(the file) for this attribute
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class FileAttributeUpdateWithoutFileException extends RestApiException
{
	private static final String ERROR_CODE = "R02";

	public FileAttributeUpdateWithoutFileException()
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

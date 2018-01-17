package org.molgenis.data.importer.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IncompatibleSystemMetadataException extends ImporterException
{
	private static final String ERROR_CODE = "I04";

	public IncompatibleSystemMetadataException()
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

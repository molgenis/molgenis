package org.molgenis.data.importer.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EmptyImportException extends ImporterException
{
	private static final String ERROR_CODE = "I06";

	public EmptyImportException()
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

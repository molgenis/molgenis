package org.molgenis.data.importer.exception;

/**
 * Thrown if "addEntity" was never called before the getAttribute, and hence the list of entities to import is empty
 */
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

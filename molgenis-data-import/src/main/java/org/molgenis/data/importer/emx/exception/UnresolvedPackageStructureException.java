package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnresolvedPackageStructureException extends EmxException
{
	private static final String ERROR_CODE = "E09";

	public UnresolvedPackageStructureException()
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

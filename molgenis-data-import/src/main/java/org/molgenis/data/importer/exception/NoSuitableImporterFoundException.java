package org.molgenis.data.importer.exception;

import static java.util.Objects.requireNonNull;

//FIXME: reasonable name, or rewrite code that throws this to actually determine what is going on
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class NoSuitableImporterFoundException extends ImporterException
{
	private static final String ERROR_CODE = "I03";
	private final String fileName;

	public NoSuitableImporterFoundException(String fileName)
	{
		super(ERROR_CODE);
		this.fileName = requireNonNull(fileName);
	}

	public String getFileName()
	{
		return fileName;
	}

	@Override
	public String getMessage()
	{
		return String.format("fileName:%s", fileName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { fileName };
	}
}

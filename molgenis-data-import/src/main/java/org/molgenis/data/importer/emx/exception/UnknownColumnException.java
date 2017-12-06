package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownColumnException extends EmxException
{
	private final static String ERROR_CODE = "E07";
	private String columnName;
	private String sheetName;

	public UnknownColumnException(String columnName, String sheetName)
	{
		super(ERROR_CODE);
		this.columnName = columnName;
		this.sheetName = sheetName;
	}

	@Override
	public String getMessage()
	{
		return String.format("columnName:%s sheet:%s", columnName, sheetName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { columnName, sheetName };
	}
}

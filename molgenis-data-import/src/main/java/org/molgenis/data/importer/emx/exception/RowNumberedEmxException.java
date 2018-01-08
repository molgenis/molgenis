package org.molgenis.data.importer.emx.exception;

public class RowNumberedEmxException extends EmxException
{
	private static final String ERROR_CODE = "E14";
	private final int rowIndex;

	public RowNumberedEmxException(int rowIndex, Exception cause)
	{
		super(ERROR_CODE, cause);
		this.rowIndex = rowIndex;
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Integer[] { rowIndex };
	}
}

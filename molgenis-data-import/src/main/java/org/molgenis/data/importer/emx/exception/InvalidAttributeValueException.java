package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidAttributeValueException extends EmxException
{
	private static final String ERROR_CODE = "E13";
	private final String attribute;
	private final String value;
	private final String sheetName;
	private final String[] allowedTypes;
	private final int rowIndex;

	public InvalidAttributeValueException(String attribute, String value, String sheetName, String[] allowedTypes,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.value = value;
		this.sheetName = sheetName;
		this.allowedTypes = allowedTypes;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s value:%s sheetName:%s allowedTypes:%s rowIndex:%d", attribute, value,
				sheetName, allowedTypes, rowIndex);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attribute, value, sheetName, String.join(",", allowedTypes), rowIndex };
	}
}

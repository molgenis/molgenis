package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidBoolAttributeValueException extends EmxException
{
	private static final String ERROR_CODE = "E02";
	private final String attributeAttributeName;
	private final String booleanString;
	private final String sheetName;
	private final int rowIndex;

	public InvalidBoolAttributeValueException(String attributeAttributeName, String booleanString, String sheetName,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.attributeAttributeName = attributeAttributeName;
		this.booleanString = booleanString;
		this.sheetName = sheetName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeAttributeName:%s booleanString:%s sheetName:%s rowIndex:%d",
				attributeAttributeName, booleanString, sheetName, rowIndex);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeAttributeName, booleanString, sheetName, rowIndex };
	}
}

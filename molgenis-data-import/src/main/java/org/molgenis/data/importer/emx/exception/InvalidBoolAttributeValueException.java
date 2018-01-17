package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidBoolAttributeValueException extends EmxException
{
	private static final String ERROR_CODE = "E02";
	private final String attributeAttributeName;
	private final String booleanString;
	private final String sheetName;

	public InvalidBoolAttributeValueException(String attributeAttributeName, String booleanString, String sheetName)
	{
		super(ERROR_CODE);
		this.attributeAttributeName = attributeAttributeName;
		this.booleanString = booleanString;
		this.sheetName = sheetName;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeAttributeName:%s booleanString:%s sheetName:%s", attributeAttributeName,
				booleanString, sheetName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeAttributeName, booleanString, sheetName };
	}
}

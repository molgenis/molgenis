package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidAttributeValueException extends EmxException
{
	private static final String ERROR_CODE = "E13";
	private final String attribute;
	private final String value;
	private final String sheetName;
	private final String[] allowedTypes;

	public InvalidAttributeValueException(String attribute, String value, String sheetName, String[] allowedTypes)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.value = value;
		this.sheetName = sheetName;
		this.allowedTypes = allowedTypes;
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s value:%s sheetName:%s allowedTypes:%s", attribute, value, sheetName,
				allowedTypes);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attribute, value, sheetName, String.join(",", allowedTypes) };
	}
}

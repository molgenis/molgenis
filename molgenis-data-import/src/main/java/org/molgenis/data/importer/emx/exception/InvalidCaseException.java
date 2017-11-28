package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidCaseException extends EmxException
{
	private static final String ERROR_CODE = "E04";
	private final String attributeName;
	private final String sheetName;
	private final String emxAttrName;

	public InvalidCaseException(String attributeName, String sheetName, String emxAttrName)
	{
		super(ERROR_CODE);
		this.attributeName = attributeName;
		this.sheetName = sheetName;
		this.emxAttrName = emxAttrName;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s sheetName:%s emxAttrName: %s", attributeName, sheetName, emxAttrName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeName, sheetName, emxAttrName };
	}
}

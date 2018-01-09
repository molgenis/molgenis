package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MissingMetadataValueException extends EmxException
{
	private static final String ERROR_CODE = "E06";
	private final String attributeAttributeName;
	private final String attributeName;
	private final String sheetName;

	public MissingMetadataValueException(String attributeAttributeName, String attributeName, String sheetName)
	{
		super(ERROR_CODE);
		this.attributeAttributeName = attributeAttributeName;
		this.attributeName = attributeName;
		this.sheetName = sheetName;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeAttribute:%s attribute:%s sheetName:%s", attributeAttributeName, attributeName,
				sheetName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeAttributeName, attributeName, sheetName };
	}
}

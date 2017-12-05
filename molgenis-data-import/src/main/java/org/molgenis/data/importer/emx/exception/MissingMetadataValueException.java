package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MissingMetadataValueException extends EmxException
{
	private static final String ERROR_CODE = "E06";
	private final String attributeAttributeName;
	private final String attributeName;
	private final String sheetName;
	private final int rowIndex;

	public MissingMetadataValueException(String attributeAttributeName, String attributeName, String sheetName,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.attributeAttributeName = attributeAttributeName;
		this.attributeName = attributeName;
		this.sheetName = sheetName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeAttribute:%s attribute:%s sheetName: %s rowIndex:%d", attributeAttributeName,
				attributeName, sheetName, rowIndex);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeAttributeName, attributeName, sheetName, rowIndex };
	}
}

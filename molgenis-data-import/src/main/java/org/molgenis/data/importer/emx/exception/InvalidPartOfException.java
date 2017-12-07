package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidPartOfException extends EmxException
{
	private static final String ERROR_CODE = "E05";
	private final String partOfAttributeAttribute;
	private final String attributeName;
	private final String entityTypeId;
	private final int rowIndex;

	public InvalidPartOfException(String partOfAttributeAttribute, String attributeName, String entityTypeId,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.partOfAttributeAttribute = partOfAttributeAttribute;
		this.attributeName = attributeName;
		this.entityTypeId = entityTypeId;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("partOfAttributeAttribute:%s attribute:%s entityTypeId: %s rowIndex:%d",
				partOfAttributeAttribute, attributeName, entityTypeId, rowIndex);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { partOfAttributeAttribute, attributeName, entityTypeId, rowIndex };
	}
}

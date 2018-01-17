package org.molgenis.data.importer.emx.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidPartOfException extends EmxException
{
	private static final String ERROR_CODE = "E05";
	private final String partOfAttributeAttribute;
	private final String attributeName;
	private final String entityTypeId;

	public InvalidPartOfException(String partOfAttributeAttribute, String attributeName, String entityTypeId)
	{
		super(ERROR_CODE);
		this.partOfAttributeAttribute = partOfAttributeAttribute;
		this.attributeName = attributeName;
		this.entityTypeId = entityTypeId;
	}

	@Override
	public String getMessage()
	{
		return String.format("partOfAttributeAttribute:%s attribute:%s entityTypeId:%s", partOfAttributeAttribute,
				attributeName, entityTypeId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { partOfAttributeAttribute, attributeName, entityTypeId };
	}
}

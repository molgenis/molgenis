package org.molgenis.data;

@SuppressWarnings({ "squid:MaximumInheritanceDepth" })
public class UnknownEntityTypeParentException extends UnknownDataException
{
	private static final String ERROR_CODE = "D08";

	private final String entityTypeId;
	private final String parentEntityTypeId;

	public UnknownEntityTypeParentException(String entityTypeId, String parentEntityTypeId)
	{
		super(ERROR_CODE);
		this.entityTypeId = entityTypeId;
		this.parentEntityTypeId = parentEntityTypeId;
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s parentId: %s", entityTypeId, parentEntityTypeId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { parentEntityTypeId, entityTypeId };
	}
}


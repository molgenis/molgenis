package org.molgenis.data;

@SuppressWarnings({ "squid:MaximumInheritanceDepth" })
public class UnknownParentException extends UnknownDataException
{
	private static final String ERROR_CODE = "D08";

	private final transient Object entityId;
	private final transient Object parentEntityId;

	public UnknownParentException(Object entityId, Object parentEntityId)
	{
		super(ERROR_CODE);
		this.entityId = entityId;
		this.parentEntityId = parentEntityId;
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s parentId: %s", entityId.toString(), parentEntityId.toString());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { parentEntityId, entityId };
	}
}


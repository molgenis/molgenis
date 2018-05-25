package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "squid:MaximumInheritanceDepth" })
public class UnknownEntityException extends UnknownDataException
{
	private static final String ERROR_CODE = "D02";

	private final transient EntityType entityType;

	private final String entityTypeId;

	private final transient Object entityId;

	public UnknownEntityException(EntityType entityType, Object entityId)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.entityTypeId = entityType.getId();
		this.entityId = requireNonNull(entityId);
	}

	public UnknownEntityException(String entityTypeId, Object entityId)
	{
		super(ERROR_CODE);
		entityType = null;
		this.entityTypeId = requireNonNull(entityTypeId);
		this.entityId = requireNonNull(entityId);
	}

	public Object getEntityId()
	{
		return entityId;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s id:%s", entityTypeId, entityId.toString());
	}

	@Override
	public String getErrorCode()
	{
		return entityType == null ? ERROR_CODE + "a" : ERROR_CODE;
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return entityType == null ? new Object[] { entityTypeId, entityId } : new Object[] { entityType, entityId };
	}
}


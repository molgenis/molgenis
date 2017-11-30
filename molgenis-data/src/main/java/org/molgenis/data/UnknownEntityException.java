package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "squid:MaximumInheritanceDepth" })
public class UnknownEntityException extends UnknownDataException
{
	private static final String ERROR_CODE = "D02";

	private final transient EntityType entityType;
	private final transient Object entityId;

	public UnknownEntityException(EntityType entityType, Object entityId)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.entityId = requireNonNull(entityId);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s id:%s", entityType.getId(), entityId.toString());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityType, entityId };
	}
}


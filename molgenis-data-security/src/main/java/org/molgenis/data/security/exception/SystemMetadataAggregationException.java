package org.molgenis.data.security.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

public class SystemMetadataAggregationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "S04";
	private final transient EntityType entityType;

	public SystemMetadataAggregationException(EntityType entityType)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s", entityType.getId());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityType };
	}
}

package org.molgenis.data.security.meta;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

public class EditSystemEntityTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "S03";
	private final String operation;
	private final EntityType entityType;

	public EditSystemEntityTypeException(String operation, EntityType entityType)
	{
		super(ERROR_CODE);
		this.operation = requireNonNull(operation);
		this.entityType = requireNonNull(entityType);
	}

	public String getOperation()
	{
		return operation;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public String getMessage()
	{
		return String.format("operation:%s entityType:%s", operation, entityType.getId());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { operation, entityType };
	}
}

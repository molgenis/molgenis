package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

public class AbstractEntityDeletionException extends CodedRuntimeException
{
	public static final String ERROR_CODE = "R12";
	private final transient EntityType entityType;

	public AbstractEntityDeletionException(EntityType entityType)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
	}

	@Override
	public String getMessage()
	{
		return String.format("entityType:%s", entityType.getId());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityType };
	}
}
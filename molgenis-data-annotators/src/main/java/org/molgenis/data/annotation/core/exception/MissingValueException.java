package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

public class MissingValueException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN06";
	private final transient Object key;
	private final transient EntityType entityType;

	public MissingValueException(Object key, EntityType entityType)
	{
		super(ERROR_CODE);
		this.key = requireNonNull(key);
		this.entityType = requireNonNull(entityType);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s key:%s", entityType.getId(), key);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityType, key };
	}
}

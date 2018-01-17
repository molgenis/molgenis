package org.molgenis.data.index.exception;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnknownIndexException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "IDX02";
	private final transient EntityType entityType;

	public UnknownIndexException(EntityType entityType, Throwable cause)
	{
		super(ERROR_CODE, cause);
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

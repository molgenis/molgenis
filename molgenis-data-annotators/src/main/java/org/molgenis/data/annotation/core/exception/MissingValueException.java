package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

public class MissingValueException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN06";
	private Object key;
	private EntityType entityType;

	public MissingValueException(Object key, EntityType entityType)
	{
		super(ERROR_CODE);
		this.key = key;
		this.entityType = entityType;
	}
}

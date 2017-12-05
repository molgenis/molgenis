package org.molgenis.data;

import static java.util.Objects.requireNonNull;

public class EntityTypeAlreadyExistsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D03";

	private final String entityTypeId;

	public EntityTypeAlreadyExistsException(String entityTypeId)
	{
		super(ERROR_CODE);
		this.entityTypeId = requireNonNull(entityTypeId);
	}

	public String getEntityTypeId()
	{
		return entityTypeId;
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", entityTypeId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityTypeId };
	}
}

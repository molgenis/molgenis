package org.molgenis.data.index.admin;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

public class RepositoryNotIndexedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C06";
	private final transient EntityType entityType;

	public RepositoryNotIndexedException(EntityType entityType)
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

package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityTypeAlreadyExistsException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V21";

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

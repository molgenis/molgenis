package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

/**
 * Thrown to indicate that existing data does not correspond to modified enum options.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EnumConstraintModificationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V09";

	private final String entityTypeId;

	public EnumConstraintModificationException(String entityTypeId, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s", entityTypeId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityTypeId };
	}
}

package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

/**
 * Thrown to indicate that existing data contains <tt>null</tt> values when creating a not-null constraint.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class NotNullConstraintCreationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V05";

	private final String entityTypeId;
	private final String attributeName;

	public NotNullConstraintCreationException(String entityTypeId, String attributeName, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s", entityTypeId, attributeName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeName, entityTypeId };
	}
}

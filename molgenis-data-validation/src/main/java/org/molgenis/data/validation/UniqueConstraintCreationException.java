package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

/**
 * Thrown to indicate that data values are not unique when creating a unique constraint.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UniqueConstraintCreationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V08";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public UniqueConstraintCreationException(String entityTypeId, String attributeName, String valueAsString,
			Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.valueAsString = valueAsString;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s value:%s", entityTypeId, attributeName, valueAsString);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityTypeId, attributeName, valueAsString };
	}
}



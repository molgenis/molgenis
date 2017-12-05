package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

/**
 * Thrown when deleting data that is still referenced by other data.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityReferenceConstraintViolationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V11";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public EntityReferenceConstraintViolationException(String entityTypeId, String attributeName, String valueAsString,
			Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.valueAsString = requireNonNull(valueAsString);
	}

	@Override
	public String getMessage()
	{
		return "type:" + entityTypeId + " attribute:" + attributeName + " value:" + valueAsString;
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityTypeId, attributeName, valueAsString };
	}
}

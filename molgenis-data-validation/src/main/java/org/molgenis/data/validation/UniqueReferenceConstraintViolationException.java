package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

/**
 * Thrown to indicate that referenced data values are not unique when updating data.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UniqueReferenceConstraintViolationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V07";

	private final String entityTypeId;
	private final String attributeName;
	private final String entityIdAsString;
	private final String valueAsString;

	public UniqueReferenceConstraintViolationException(String entityTypeId, String attributeName,
			String entityIdAsString, String valueAsString, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.entityIdAsString = requireNonNull(entityIdAsString);
		this.valueAsString = valueAsString;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s entity:%s value:%s", entityTypeId, attributeName, entityIdAsString,
				valueAsString);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityTypeId, attributeName, entityIdAsString, valueAsString };
	}
}


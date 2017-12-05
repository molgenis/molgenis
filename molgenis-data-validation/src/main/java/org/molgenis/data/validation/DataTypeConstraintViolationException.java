package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

/**
 * Thrown to indicate that a data value is not of the required type when updating data.
 * // TODO discuss: extend from TypeMismatchDataAccessException instead of DataIntegrityViolationException
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DataTypeConstraintViolationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V03";

	private final String valueAsString;
	private final String type; // TODO replace String with List<AttributeType>

	public DataTypeConstraintViolationException(String valueAsString, String type, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.valueAsString = requireNonNull(valueAsString);
		this.type = requireNonNull(type);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s value:%s", type, valueAsString);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { valueAsString, type };
	}
}

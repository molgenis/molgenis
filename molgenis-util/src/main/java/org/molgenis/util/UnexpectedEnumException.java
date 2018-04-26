package org.molgenis.util;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Exception that is thrown in the default section of a switch statement as a defensive programming strategy.
 */
public class UnexpectedEnumException extends RuntimeException
{
	private static final String UNEXPECTED_ENUM_CONSTANT_FORMAT = "Unexpected enum constant '%s' for type '%s'";

	private final Enum enumConstant;

	public <E extends Enum> UnexpectedEnumException(E enumConstant)
	{
		this.enumConstant = requireNonNull(enumConstant);
	}

	@Override
	public String getMessage()
	{
		return format(UNEXPECTED_ENUM_CONSTANT_FORMAT, enumConstant.name(), enumConstant.getClass().getSimpleName());
	}
}

package org.molgenis.util;

import static java.lang.String.format;

/**
 * Exception that is thrown in the default section of a switch statement as a defensive programming strategy.
 */
public class UnexpectedEnumException extends RuntimeException
{
	private static final String UNEXPECTED_ENUM_CONSTANT_FORMAT = "Unexpected enum constant '%s' for type '%s'";

	public <E extends Enum> UnexpectedEnumException(E enumConstant)
	{
		super(format(UNEXPECTED_ENUM_CONSTANT_FORMAT, enumConstant.name(), enumConstant.getClass().getSimpleName()));
	}
}

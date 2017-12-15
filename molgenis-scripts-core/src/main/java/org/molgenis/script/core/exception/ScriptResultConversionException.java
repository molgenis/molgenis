package org.molgenis.script.core.exception;

import org.molgenis.i18n.CodedRuntimeException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Occurs when a script result cannot be converted to the appropriate type.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class ScriptResultConversionException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "SC01";

	private final Object value;

	public ScriptResultConversionException(Object value)
	{
		super(ERROR_CODE);
		this.value = value;
	}

	public ScriptResultConversionException(Object value, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.value = requireNonNull(value);
	}

	@Override
	public String getMessage()
	{
		return format("value:%s", value);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { value };
	}
}
package org.molgenis.script.core.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Throw when an unknown ScriptType is requested.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class UnknownScriptTypeException extends ScriptException
{
	private static final String ERROR_CODE = "SC01";

	private final String type;

	public UnknownScriptTypeException(String type)
	{
		super(ERROR_CODE);
		this.type = requireNonNull(type);
	}

	public String getType()
	{
		return type;
	}

	@Override
	public String getMessage()
	{
		return format("type:%s", type);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { type };
	}
}

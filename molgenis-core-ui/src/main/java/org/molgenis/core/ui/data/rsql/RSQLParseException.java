package org.molgenis.core.ui.data.rsql;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class RSQLParseException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C02";
	private final String rsql;

	public RSQLParseException(String rsql, Throwable cause)
	{
		super(ERROR_CODE);
		this.rsql = requireNonNull(rsql);
	}

	@Override
	public String getMessage()
	{
		return String.format("rsql:%s", rsql);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { rsql };
	}
}

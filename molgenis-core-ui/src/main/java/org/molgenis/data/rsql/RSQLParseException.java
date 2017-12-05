package org.molgenis.data.rsql;

import org.molgenis.data.CodedRuntimeException;

public class RSQLParseException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C02";
	private final String rsql;

	public RSQLParseException(String rsql)
	{
		super(ERROR_CODE);
		this.rsql = rsql;
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

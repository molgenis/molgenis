package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

import static java.util.Objects.requireNonNull;

public class DateParseException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D11";
	private final Attribute attribute;
	private final String value;

	public DateParseException(Attribute attribute, String value)
	{
		super(ERROR_CODE);
		this.attribute = requireNonNull(attribute);
		this.value = requireNonNull(value);
	}

	public Attribute getAttribute()
	{
		return attribute;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s value:%s", attribute.getName(), value);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attribute.getName(), value };
	}
}

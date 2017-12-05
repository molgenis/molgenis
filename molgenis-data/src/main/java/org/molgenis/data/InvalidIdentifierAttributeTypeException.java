package org.molgenis.data;

import org.molgenis.data.meta.AttributeType;

import static java.util.Objects.requireNonNull;

public class InvalidIdentifierAttributeTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D09";
	private final AttributeType type;

	public InvalidIdentifierAttributeTypeException(AttributeType type)
	{
		super(ERROR_CODE);
		this.type = requireNonNull(type);
	}

	public AttributeType getType()
	{
		return type;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeType:%s", type.name());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { type.name() };
	}
}

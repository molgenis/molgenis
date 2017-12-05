package org.molgenis.data.rest.exception;

import org.molgenis.data.meta.AttributeType;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * thrown of a operation is attempted on a attribute type on which this operation is not supported
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IllegalAttributeTypeException extends RestApiException
{
	private final static String ERROR_CODE = "R08";
	private AttributeType attributeType;

	public IllegalAttributeTypeException(AttributeType attributeType)
	{
		super(ERROR_CODE);
		this.attributeType = requireNonNull(attributeType);
	}

	public AttributeType getAttributeType()
	{
		return attributeType;
	}

	@Override
	public String getMessage()
	{
		return format("attributeType:%s", attributeType.name());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeType.name() };
	}
}

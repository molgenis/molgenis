package org.molgenis.data.rest.exception;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * thrown of a operation is attempted on a attribute type on which this operation is not supported
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IllegalAttributeTypeException extends RestApiException
{
	private static final String ERROR_CODE = "R08";
	private final Attribute attribute;
	private final AttributeType attributeType;

	public IllegalAttributeTypeException(Attribute attribute, AttributeType attributeType)
	{
		super(ERROR_CODE);
		this.attribute = requireNonNull(attribute);
		this.attributeType = requireNonNull(attributeType);
	}

	@Override
	public String getMessage()
	{
		return format("type:%s attribute:%s type:%s", attribute.getEntity().getId(), attribute.getName(),
				attributeType.name());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attribute.getEntity(), attribute, attributeType.toString() };
	}
}

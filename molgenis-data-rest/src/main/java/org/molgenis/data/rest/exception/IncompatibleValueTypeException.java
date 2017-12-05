package org.molgenis.data.rest.exception;

import org.molgenis.data.meta.model.Attribute;

import static java.util.Objects.requireNonNull;

/**
 * thrown if a conversion form one attribute type to another was attempted but there was a value that was not suitable for the new type
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IncompatibleValueTypeException extends RestApiException
{
	private static final String ERROR_CODE = "R01";
	private final transient Attribute attribute;
	private final String type;
	private final String[] expectedTypes;

	public IncompatibleValueTypeException(Attribute attribute, String type, String[] expectedTypes)
	{
		super(ERROR_CODE);
		this.attribute = requireNonNull(attribute);
		this.type = requireNonNull(type);
		this.expectedTypes = requireNonNull(expectedTypes);
	}

	public Attribute getAttribute()
	{
		return attribute;
	}

	public String getType()
	{
		return type;
	}

	public String[] getExpectedTypes()
	{
		return expectedTypes;
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s type:%s, expectedTypes:%s", attribute.getName(), type,
				String.join(",", expectedTypes));
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attribute.getName(), type, String.join(",", expectedTypes) };
	}
}

package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.AttributeType;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidAttributeTypeException extends EmxException
{
	private static final String ERROR_CODE = "E10";
	private final String attributeAttribute;
	private final AttributeType attributeType;
	private final String attribute;
	private final String[] validOptions;

	public InvalidAttributeTypeException(String attributeAttribute, AttributeType attributeType, String attribute,
			String[] validOptions)
	{
		super(ERROR_CODE);
		this.attributeAttribute = attributeAttribute;
		this.attributeType = attributeType;
		this.attribute = attribute;
		this.validOptions = validOptions;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeAttribute:%s attributeType:%s attribute:%s validOptions:%s", attributeType,
				attributeAttribute, attribute, String.join(",", validOptions));
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeAttribute, attributeType, attribute, validOptions };
	}
}

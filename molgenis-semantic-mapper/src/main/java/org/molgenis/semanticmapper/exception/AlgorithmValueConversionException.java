package org.molgenis.semanticmapper.exception;

import org.molgenis.data.meta.AttributeType;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Thrown on mismatch between algorithm value and attribute type.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class AlgorithmValueConversionException extends MappingServiceException
{
	private static final String ERROR_CODE = "M03";
	private final transient Object value;
	private final AttributeType attributeType;

	public AlgorithmValueConversionException(Object value, AttributeType attributeType)
	{
		super(ERROR_CODE);
		this.value = requireNonNull(value);
		this.attributeType = requireNonNull(attributeType);
	}

	public AlgorithmValueConversionException(Object value, AttributeType attributeType, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.value = requireNonNull(value);
		this.attributeType = requireNonNull(attributeType);
	}

	public Object getValue()
	{
		return value;
	}

	public AttributeType getAttributeType()
	{
		return attributeType;
	}

	@Override
	public String getMessage()
	{
		return format("value:%s type:%s", value.toString(), attributeType.toString());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { value.toString(), attributeType.name() };
	}
}

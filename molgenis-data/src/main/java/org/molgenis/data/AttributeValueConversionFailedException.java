package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

import static java.util.Objects.requireNonNull;

/**
 * Thrown on mismatch between value type and attribute type.
 */
public class AttributeValueConversionFailedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D10";

	private final transient Attribute attr;
	private final transient Object value;

	public AttributeValueConversionFailedException(Attribute attr, Object value, Exception exception)
	{
		super(ERROR_CODE, exception);
		this.attr = requireNonNull(attr);
		this.value = requireNonNull(value);
	}

	public AttributeValueConversionFailedException(Attribute attr, Object value)
	{
		super(ERROR_CODE);
		this.attr = requireNonNull(attr);
		this.value = requireNonNull(value);
	}

	public Attribute getAttr()
	{
		return attr;
	}

	public Object getValue()
	{
		return value;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s expected:%s actual:%s value:%s", attr.getEntity().getId(),
				attr.getName(), attr.getDataType().name(), value.getClass().getName(), value.toString());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attr.getEntity(), attr, value, attr.getDataType() };
	}
}

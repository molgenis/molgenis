package org.molgenis.data;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class DateParseException extends DataConversionException
{
	private static final String ERROR_CODE_DATE = "D11a";
	private static final String ERROR_CODE_DATE_TIME = "D11b";

	private final Attribute attribute;
	private final String value;

	public DateParseException(Attribute attribute, String value)
	{
		super(attribute.getDataType() == AttributeType.DATE ? ERROR_CODE_DATE : ERROR_CODE_DATE_TIME);
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

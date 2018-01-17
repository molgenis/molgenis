package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownAttributeTypeException extends EmxException
{
	private static final String ERROR_CODE = "E03";
	private final String emxDataType;
	private final transient Attribute attribute;

	public UnknownAttributeTypeException(String emxDataType, Attribute attribute)
	{
		super(ERROR_CODE);
		this.emxDataType = requireNonNull(emxDataType);
		this.attribute = requireNonNull(attribute);
	}

	@Override
	public String getMessage()
	{
		return String.format("dataType:%s attribute:%s", emxDataType, attribute.getName());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { emxDataType, attribute.getName() };
	}
}

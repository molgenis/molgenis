package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritcanceDepth")
public class UnknownAttributeTypeException extends EmxException
{
	private static final String ERROR_CODE = "E03";
	private final String emxDataType;
	private final Attribute attribute;
	private final int rowIndex;

	public UnknownAttributeTypeException(String emxDataType, Attribute attribute, int rowIndex)
	{
		super(ERROR_CODE);
		this.emxDataType = requireNonNull(emxDataType);
		this.attribute = requireNonNull(attribute);
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("dataType:%s attribute:%s rowIndex:%d", emxDataType, attribute.getName(), rowIndex);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { emxDataType, attribute.getName(), rowIndex };
	}
}

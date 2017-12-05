package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownMappedByException extends EmxException
{
	private static final String ERROR_CODE = "E08";
	private final Attribute attribute;
	private final String mappedByAttributeName;
	private final int rowIndex;

	public UnknownMappedByException(Attribute attribute, String mappedByAttributeName, int rowIndex)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.mappedByAttributeName = mappedByAttributeName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s referenceName:%s, rowIndex:%d", attribute.getName(),
				mappedByAttributeName,
				rowIndex);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { mappedByAttributeName, attribute.getName(), rowIndex };
	}
}

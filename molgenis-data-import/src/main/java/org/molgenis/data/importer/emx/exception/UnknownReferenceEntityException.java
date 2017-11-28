package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownReferenceEntityException extends EmxException
{
	private static final String ERROR_CODE = "E12";
	private final Attribute attribute;
	private final String refEntityName;
	private final int rowIndex;

	public UnknownReferenceEntityException(Attribute attribute, String refEntityName, int rowIndex)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.refEntityName = refEntityName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s referenceName:%s, rowIndex:%d", attribute.getName(), refEntityName,
				rowIndex);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { refEntityName, attribute.getName(), rowIndex };
	}
}

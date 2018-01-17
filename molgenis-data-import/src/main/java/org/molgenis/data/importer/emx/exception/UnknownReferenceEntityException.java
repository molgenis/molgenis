package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownReferenceEntityException extends EmxException
{
	private static final String ERROR_CODE = "E12";
	private final Attribute attribute;
	private final String refEntityName;

	public UnknownReferenceEntityException(Attribute attribute, String refEntityName)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.refEntityName = refEntityName;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s referenceName:%s", attribute.getName(), refEntityName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { refEntityName, attribute.getName() };
	}
}

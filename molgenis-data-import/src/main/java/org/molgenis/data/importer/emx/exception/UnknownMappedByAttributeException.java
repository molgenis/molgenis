package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownMappedByAttributeException extends EmxException
{
	private static final String ERROR_CODE = "E08";
	private final Attribute attribute;
	private final String mappedByAttributeName;

	public UnknownMappedByAttributeException(Attribute attribute, String mappedByAttributeName)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.mappedByAttributeName = mappedByAttributeName;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s referenceName:%s", attribute.getName(), mappedByAttributeName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { mappedByAttributeName, attribute.getName() };
	}
}

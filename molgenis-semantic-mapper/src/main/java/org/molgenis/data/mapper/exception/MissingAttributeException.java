package org.molgenis.data.mapper.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class MissingAttributeException extends IncompatibleTargetException
{
	private static final String ERROR_CODE = "M04";
	private final String attributeName;

	public MissingAttributeException(String attributeName)
	{
		super(ERROR_CODE);
		this.attributeName = requireNonNull(attributeName);
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	@Override
	public String getMessage()
	{
		return format("name:%s", attributeName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { attributeName };
	}
}

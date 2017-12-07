package org.molgenis.data.mapper.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * When comparing the metadata of two EntityTypes and one of the attributes is not contained in the target repository,
 * use this exception.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class MissingTargetAttributeException extends IncompatibleTargetException
{
	private static final String ERROR_CODE = "M04";
	private final String attributeName;

	public MissingTargetAttributeException(String attributeName)
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

package org.molgenis.data.rest.exception;

import static java.util.Objects.requireNonNull;

/**
 * expand is of form 'attr1', 'entity1[attr1]', 'entity1[attr1;attr2]'
 * if it is not, then this exception is thrown
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidExpandException extends RestApiException
{
	private static final String ERROR_CODE = "R03";
	private final Object expand;

	public InvalidExpandException(String expand)
	{
		super(ERROR_CODE);
		this.expand = requireNonNull(expand);
	}

	public Object getExpand()
	{
		return expand;
	}

	@Override
	public String getMessage()
	{
		return String.format("expand:%s", expand);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { expand };
	}
}

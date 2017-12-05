package org.molgenis.data.mapper.exception;

/**
 * Use when an mapping algorithm is applied to a null value.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class AlgorithmNullValueException extends MappingServiceException
{
	private static final String ERROR_CODE = "M08";

	public AlgorithmNullValueException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "null";
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}

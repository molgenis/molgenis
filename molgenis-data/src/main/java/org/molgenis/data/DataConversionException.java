package org.molgenis.data;

/**
 *
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public abstract class DataConversionException extends CodedRuntimeException
{
	public DataConversionException(String errorCode)
	{
		super(errorCode);
	}

	public DataConversionException(String errorCode, Throwable cause)
	{
		super(errorCode, cause);
	}
}

package org.molgenis.data.mapper.exception;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public abstract class IncompatibleTargetException extends MappingServiceException
{
	IncompatibleTargetException(String errorCode)
	{
		super(errorCode);
	}
}

package org.molgenis.data.mapper.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class IncompatibleTargetException extends MappingServiceException
{
	IncompatibleTargetException(String errorCode)
	{
		super(errorCode);
	}
}

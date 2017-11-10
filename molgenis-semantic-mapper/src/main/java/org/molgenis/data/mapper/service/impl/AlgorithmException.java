package org.molgenis.data.mapper.service.impl;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class AlgorithmException extends RuntimeException
{
	public AlgorithmException(String message)
	{
		super(message);
	}
}

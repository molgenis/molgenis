package org.molgenis.semanticmapper.service.impl;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class AlgorithmException extends RuntimeException
{
	public AlgorithmException(String message)
	{
		super(message);
	}
}

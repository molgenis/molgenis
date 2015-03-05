package org.molgenis.exceptions;

public class SearchWindowTooBigException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1482154709267669866L;

	public SearchWindowTooBigException()
	{

	}

	public SearchWindowTooBigException(String msg)
	{
		super(msg);
	}
}

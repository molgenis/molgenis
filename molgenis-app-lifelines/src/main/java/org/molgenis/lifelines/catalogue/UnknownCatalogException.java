package org.molgenis.lifelines.catalogue;

public class UnknownCatalogException extends Exception
{
	private static final long serialVersionUID = 1L;

	public UnknownCatalogException()
	{
		super();
	}

	public UnknownCatalogException(String message, Throwable t)
	{
		super(message, t);
	}

	public UnknownCatalogException(String message)
	{
		super(message);
	}

	public UnknownCatalogException(Throwable t)
	{
		super(t);
	}

}

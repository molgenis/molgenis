package org.molgenis.data.index.exception;

public class UnknownIndexInternalException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private String[] indexNames;

	public UnknownIndexInternalException(String[] indexNames)
	{
		this.indexNames = indexNames;
	}

	@Override
	public String getMessage()
	{
		return String.format("Unknown indices: [%s]", indexNames);
	}

	public String[] getIndexNames()
	{
		return indexNames;
	}
}

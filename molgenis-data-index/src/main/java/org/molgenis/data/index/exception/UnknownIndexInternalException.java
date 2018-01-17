package org.molgenis.data.index.exception;

import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.requireNonNull;

public class UnknownIndexInternalException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private String[] indexNames;

	public UnknownIndexInternalException(String[] indexNames)
	{
		this.indexNames = requireNonNull(indexNames);
	}

	@Override
	public String getMessage()
	{
		String indicesString = StringUtils.join(indexNames, ",");
		return String.format("Unknown indices: [%s]", indicesString);
	}

	public String[] getIndexNames()
	{
		return indexNames;
	}
}

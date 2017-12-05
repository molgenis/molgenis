package org.molgenis.data.index.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class IndexAlreadyExistsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "IDX01";
	private final String indexName;

	public IndexAlreadyExistsException(String indexName)
	{
		super(ERROR_CODE);
		this.indexName = requireNonNull(indexName);
	}

	@Override
	public String getMessage()
	{
		return String.format("indexName:%s", indexName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { indexName };
	}
}

package org.molgenis.data.index.exception;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class IndexAlreadyExistsException extends IndexException
{
	private static final long serialVersionUID = 1L;

	public IndexAlreadyExistsException(String indexName)
	{
		super(String.format("Index '%s' already exists.", indexName));
	}
}

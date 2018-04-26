package org.molgenis.data.index.exception;

import org.molgenis.data.MolgenisDataException;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class IndexException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public IndexException(String msg)
	{
		super(msg);
	}

	public IndexException(String msg, Throwable t)
	{
		super(msg, t);
	}
}

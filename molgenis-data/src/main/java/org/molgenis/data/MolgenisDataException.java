package org.molgenis.data;

import org.springframework.dao.DataAccessException;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class MolgenisDataException extends DataAccessException
{
	private static final long serialVersionUID = 4738825795930038340L;

	public MolgenisDataException()
	{
		this("");
	}

	public MolgenisDataException(String msg)
	{
		super(msg);
	}

	public MolgenisDataException(Throwable t)
	{
		this("", t);
	}

	public MolgenisDataException(String msg, Throwable t)
	{
		super(msg, t);
	}

}

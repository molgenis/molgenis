package org.molgenis.core.ui.style;

import java.io.IOException;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
public class MolgenisStyleException extends Exception
{
	public MolgenisStyleException(String s)
	{
		super(s);
	}

	public MolgenisStyleException(String s, IOException e)
	{
		super(s, e);
	}
}

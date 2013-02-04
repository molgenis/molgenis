package org.molgenis.framework.ui.html;

public class HtmlInputException extends Exception
{
	private static final long serialVersionUID = 3593070943464340603L;

	public HtmlInputException(String string)
	{
		super(string);
	}

	public HtmlInputException(Exception e)
	{
		super(e);
	}

}

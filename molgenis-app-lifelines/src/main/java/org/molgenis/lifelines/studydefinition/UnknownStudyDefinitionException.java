package org.molgenis.lifelines.studydefinition;

public class UnknownStudyDefinitionException extends Exception
{
	private static final long serialVersionUID = 1L;

	public UnknownStudyDefinitionException()
	{
	}

	public UnknownStudyDefinitionException(String message)
	{
		super(message);
	}

	public UnknownStudyDefinitionException(Throwable t)
	{
		super(t);

	}

	public UnknownStudyDefinitionException(String message, Throwable t)
	{
		super(message, t);
	}

}

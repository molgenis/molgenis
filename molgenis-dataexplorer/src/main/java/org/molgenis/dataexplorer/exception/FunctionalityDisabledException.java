package org.molgenis.dataexplorer.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class FunctionalityDisabledException extends CodedRuntimeException
{
	private final String functionality;
	private static final String ERROR_CODE = "DE01";


	public FunctionalityDisabledException(String functionality)
	{
		super(ERROR_CODE);
		this.functionality = requireNonNull(functionality);
	}

	public String getFunctionality()
	{
		return functionality;
	}

	@Override
	public String getMessage()
	{
		return String.format("Funcionality:%s", functionality);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { functionality };
	}
}

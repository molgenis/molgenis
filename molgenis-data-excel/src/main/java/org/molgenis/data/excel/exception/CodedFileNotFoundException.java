package org.molgenis.data.excel.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class CodedFileNotFoundException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "XLS02";
	private final String name;

	public CodedFileNotFoundException(String name)
	{
		super(ERROR_CODE);
		this.name = requireNonNull(name);
	}

	@Override
	public String getMessage()
	{
		return String.format("sheet:%s", name);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { name };
	}

}

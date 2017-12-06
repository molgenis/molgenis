package org.molgenis.data.importer.wizard;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class RepositoryAlreadyExistsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C07";
	private final String name;

	public RepositoryAlreadyExistsException(String name)
	{
		super(ERROR_CODE);
		this.name = requireNonNull(name);
	}

	@Override
	public String getMessage()
	{
		return String.format("name:%s", name);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { name };
	}
}

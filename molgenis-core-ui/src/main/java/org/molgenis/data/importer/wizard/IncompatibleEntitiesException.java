package org.molgenis.data.importer.wizard;

import org.molgenis.data.CodedRuntimeException;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class IncompatibleEntitiesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C08";
	private final List<String> entitiesNotImportable;

	public IncompatibleEntitiesException(List<String> entitiesNotImportable)
	{
		super(ERROR_CODE);
		this.entitiesNotImportable = requireNonNull(entitiesNotImportable);
	}

	@Override
	public String getMessage()
	{
		return String.format("entitiesNotImportable:%s", entitiesNotImportable);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entitiesNotImportable };
	}
}

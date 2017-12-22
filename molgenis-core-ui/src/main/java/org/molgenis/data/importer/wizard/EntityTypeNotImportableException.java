package org.molgenis.data.importer.wizard;

import org.molgenis.i18n.CodedRuntimeException;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class EntityTypeNotImportableException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C08";

	private final List<String> entitiesNotImportable;

	public EntityTypeNotImportableException(List<String> entityTypeIds)
	{
		super(ERROR_CODE);
		this.entitiesNotImportable = requireNonNull(entityTypeIds);
	}

	@Override
	public String getMessage()
	{
		return String.format("ids:%s", entitiesNotImportable.stream().collect(joining(",")));
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entitiesNotImportable.stream().collect(joining(", ")) };
	}
}

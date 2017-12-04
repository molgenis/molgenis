package org.molgenis.data.importer.wizard;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class IncompatibleEntitiesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C08";
	private List<String> entitiesNotImportable;

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
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, entitiesNotImportable);
		}).orElse(super.getLocalizedMessage());
	}
}

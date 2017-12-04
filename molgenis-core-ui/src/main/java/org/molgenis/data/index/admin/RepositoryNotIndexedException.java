package org.molgenis.data.index.admin;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class RepositoryNotIndexedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C06";
	private EntityType entityType;

	public RepositoryNotIndexedException(EntityType entityType)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
	}

	@Override
	public String getMessage()
	{
		return String.format("entityType:%s", entityType.getId());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, entityType.getLabel(language));
		}).orElse(super.getLocalizedMessage());
	}
}

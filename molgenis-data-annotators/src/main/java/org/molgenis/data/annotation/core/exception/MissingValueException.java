package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingValueException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN06";
	private Object key;
	private EntityType entityType;

	public MissingValueException(Object key, EntityType entityType)
	{
		super(ERROR_CODE);
		this.key = requireNonNull(key);
		this.entityType = requireNonNull(entityType);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s key:%s", entityType.getId(), key);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, key, entityType.getLabel(language));
		}).orElse(super.getLocalizedMessage());
	}
}

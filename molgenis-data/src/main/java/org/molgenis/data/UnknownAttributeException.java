package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownAttributeException extends UnknownDataException
{
	private static final String ERROR_CODE = "D04";
	private final EntityType entityType;
	private final String attributeName;

	public UnknownAttributeException(EntityType entityType, String attributeName)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.attributeName = requireNonNull(attributeName);
	}

	@Override
	public String getMessage()
	{
		return "type:" + entityType.getId() + " attribute:" + attributeName;
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String languageCode = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(languageService.getString(ERROR_CODE), entityType.getLabel(languageCode),
					attributeName);
		}).orElse(super.getLocalizedMessage());
	}
}

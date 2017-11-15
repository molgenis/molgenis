package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.validation.DataIntegrityViolationException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown when an attribute has a default value and is labeled as unique.
 */
public class AttributeDefaultNotUniqueConstraintViolationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V12";
	private final Attribute attribute;

	public AttributeDefaultNotUniqueConstraintViolationException(Attribute attribute)
	{
		super(ERROR_CODE);
		this.attribute = requireNonNull(attribute);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s", attribute.getEntity().getId(), attribute.getName());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String languageCode = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(languageService.getString(ERROR_CODE),
					attribute.getEntityType().getLabel(languageCode), attribute.getLabel(languageCode));
		}).orElse(super.getLocalizedMessage());
	}
}

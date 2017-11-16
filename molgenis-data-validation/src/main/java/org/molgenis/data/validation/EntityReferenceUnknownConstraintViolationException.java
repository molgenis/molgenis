package org.molgenis.data.validation;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown when updating data that references unexisting data.
 */
public class EntityReferenceUnknownConstraintViolationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V01";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public EntityReferenceUnknownConstraintViolationException(String entityTypeId, String attributeName,
			String valueAsString, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.valueAsString = requireNonNull(valueAsString);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s value: %s", entityTypeId, attributeName, valueAsString);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(
				languageService -> MessageFormat.format(languageService.getString(ERROR_CODE), valueAsString,
						attributeName, entityTypeId)).orElse(super.getLocalizedMessage());
	}
}

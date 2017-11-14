package org.molgenis.data.validation;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown to indicate that data values are not unique when updating data.
 */
public class UniqueConstraintViolationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V06";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public UniqueConstraintViolationException(String entityTypeId, String attributeName, String valueAsString,
			Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.valueAsString = valueAsString;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s value:%s", entityTypeId, attributeName, valueAsString);
	}

	@Override
	public String getLocalizedMessage()
	{
		String format = getLanguageService().getString(ERROR_CODE);
		return MessageFormat.format(format, entityTypeId, attributeName, valueAsString);
	}
}


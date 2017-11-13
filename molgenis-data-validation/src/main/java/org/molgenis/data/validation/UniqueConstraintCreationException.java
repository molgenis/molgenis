package org.molgenis.data.validation;

import org.molgenis.data.ErrorCoded;
import org.molgenis.data.MolgenisDataAccessException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown to indicate that data values are not unique when creating a unique constraint.
 */
public class UniqueConstraintCreationException extends MolgenisDataAccessException implements ErrorCoded
{
	private static final String ERROR_CODE = "V08";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public UniqueConstraintCreationException(String entityTypeId, String attributeName, String valueAsString,
			Throwable cause)
	{
		super(cause);
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
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .map(format -> MessageFormat.format(format, entityTypeId, attributeName,
										   valueAsString))
								   .orElse(super.getLocalizedMessage());
	}

	@Override
	public String getErrorCode()
	{
		return ERROR_CODE;
	}
}



package org.molgenis.data.validation;

import org.molgenis.data.ErrorCoded;
import org.molgenis.data.MolgenisDataAccessException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown to indicate that a data value is not of the required type when updating data.
 */
public class DataTypeConstraintViolationException extends MolgenisDataAccessException implements ErrorCoded
{
	private static final String ERROR_CODE = "V03";

	private final String valueAsString;
	private final String type; // TODO replace String with List<AttributeType>

	public DataTypeConstraintViolationException(String valueAsString, String type, Throwable cause)
	{
		super(cause);
		this.valueAsString = requireNonNull(valueAsString);
		this.type = requireNonNull(type);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s value:%s", type, valueAsString);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .map(format -> MessageFormat.format(format, valueAsString, type))
								   .orElse(super.getLocalizedMessage());
	}

	@Override
	public String getErrorCode()
	{
		return ERROR_CODE;
	}
}

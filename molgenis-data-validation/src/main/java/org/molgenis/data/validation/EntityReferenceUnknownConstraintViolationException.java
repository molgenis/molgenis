package org.molgenis.data.validation;

import org.molgenis.data.ErrorCoded;
import org.molgenis.data.MolgenisDataAccessException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown when updating data that references unexisting data.
 */
public class EntityReferenceUnknownConstraintViolationException extends MolgenisDataAccessException
		implements ErrorCoded
{
	private static final String ERROR_CODE = "V01";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public EntityReferenceUnknownConstraintViolationException(String entityTypeId, String attributeName,
			String valueAsString,
			Throwable cause)
	{
		super(cause);
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
		String format = getLanguageService().getString(ERROR_CODE);
		return MessageFormat.format(format, valueAsString, attributeName, entityTypeId);
	}

	@Override
	public String getErrorCode()
	{
		return ERROR_CODE;
	}
}

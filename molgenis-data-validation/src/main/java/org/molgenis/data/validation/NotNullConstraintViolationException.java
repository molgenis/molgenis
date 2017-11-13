package org.molgenis.data.validation;

import org.molgenis.data.ErrorCoded;
import org.molgenis.data.MolgenisDataAccessException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class NotNullConstraintViolationException extends MolgenisDataAccessException implements ErrorCoded
{
	private static final String ERROR_CODE = "V04";

	private final String entityTypeId;
	private final String attributeName;

	public NotNullConstraintViolationException(String entityTypeId, String attributeName, Throwable cause)
	{
		super(cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s attribute:%s", entityTypeId, attributeName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .map(format -> MessageFormat.format(format, attributeName, entityTypeId))
								   .orElse(super.getLocalizedMessage());
	}

	@Override
	public String getErrorCode()
	{
		return ERROR_CODE;
	}
}

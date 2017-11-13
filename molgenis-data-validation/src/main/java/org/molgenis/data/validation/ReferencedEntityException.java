package org.molgenis.data.validation;

import org.molgenis.data.ErrorCoded;
import org.molgenis.data.MolgenisDataAccessException;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class ReferencedEntityException extends MolgenisDataAccessException implements ErrorCoded
{
	private static final String ERROR_CODE = "V02";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public ReferencedEntityException(String entityTypeId, String attributeName, String valueAsString, Throwable cause)
	{
		super(cause);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.attributeName = requireNonNull(attributeName);
		this.valueAsString = requireNonNull(valueAsString);
	}

	@Override
	public String getMessage()
	{
		return "type:" + entityTypeId + " attribute:" + attributeName + " value:" + valueAsString;
	}

	@Override
	public String getLocalizedMessage()
	{
		String format = getLanguageService().getBundle().getString(ERROR_CODE);
		return format(format, valueAsString, attributeName, entityTypeId);
	}

	@Override
	public String getErrorCode()
	{
		return ERROR_CODE;
	}
}

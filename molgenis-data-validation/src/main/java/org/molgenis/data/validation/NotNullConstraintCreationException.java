package org.molgenis.data.validation;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown to indicate that existing data contains <tt>null</tt> values when creating a not-null constraint.
 */
public class NotNullConstraintCreationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V05";

	private final String entityTypeId;
	private final String attributeName;

	public NotNullConstraintCreationException(String entityTypeId, String attributeName, Throwable cause)
	{
		super(ERROR_CODE, cause);
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
		String format = getLanguageService().getString(ERROR_CODE);
		return MessageFormat.format(format, attributeName, entityTypeId);
	}
}

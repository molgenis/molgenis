package org.molgenis.data.validation;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown when deleting data that is still referenced by other data.
 */
public class EntityReferenceConstraintViolationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V11";

	private final String entityTypeId;
	private final String attributeName;
	private final String valueAsString;

	public EntityReferenceConstraintViolationException(String entityTypeId, String attributeName, String valueAsString,
			Throwable cause)
	{
		super(ERROR_CODE, cause);
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
		String format = getLanguageService().getString(ERROR_CODE);
		return format(format, entityTypeId, attributeName, valueAsString);
	}
}

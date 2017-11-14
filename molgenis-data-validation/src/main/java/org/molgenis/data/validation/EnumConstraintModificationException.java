package org.molgenis.data.validation;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown to indicate that existing data does not correspond to modified enum options.
 */
public class EnumConstraintModificationException extends DataIntegrityViolationException
{
	private static final String ERROR_CODE = "V09";

	private final String entityTypeId;

	public EnumConstraintModificationException(String entityTypeId, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.entityTypeId = requireNonNull(entityTypeId);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s", entityTypeId);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(
				languageService -> MessageFormat.format(languageService.getString(ERROR_CODE), entityTypeId))
								   .orElse(super.getLocalizedMessage());
	}
}

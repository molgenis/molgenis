package org.molgenis.data.rest.exception;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * expand is of form 'attr1', 'entity1[attr1]', 'entity1[attr1;attr2]'
 * if it is not, than this exception is thrown
 */
public class InvalidExpandException extends RestApiException
{
	private final static String ERROR_CODE = "R03";
	private Object expand;

	public InvalidExpandException(String expand)
	{
		super(ERROR_CODE);
		this.expand = requireNonNull(expand);
	}

	@Override
	public String getMessage()
	{
		return String.format("expand:", expand);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, expand);
		}).orElse(super.getLocalizedMessage());
	}
}

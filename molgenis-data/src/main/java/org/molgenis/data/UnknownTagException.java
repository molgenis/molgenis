package org.molgenis.data;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:S1948")
public class UnknownTagException extends UnknownDataException
{
	private static final String ERROR_CODE = "D10";

	private final Object tagId;

	public UnknownTagException(Object tagId)
	{
		super(ERROR_CODE);
		this.tagId = requireNonNull(tagId);
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", tagId.toString());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, tagId.toString());
		}).orElse(super.getLocalizedMessage());
	}
}


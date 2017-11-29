package org.molgenis.data.mapper.exception;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class MissingAttributeException extends IncompatibleTargetException
{
	private static final String ERROR_CODE = "M04";
	private final String attributeName;

	public MissingAttributeException(String attributeName)
	{
		super(ERROR_CODE);
		this.attributeName = requireNonNull(attributeName);
	}

	public String getAttributeName()
	{
		return attributeName;
	}

	@Override
	public String getMessage()
	{
		return format("name:%s", attributeName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attributeName);
		}).orElseGet(super::getLocalizedMessage);
	}
}

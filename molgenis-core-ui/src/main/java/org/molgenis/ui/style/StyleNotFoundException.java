package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class StyleNotFoundException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "C10";
	private String styleName;

	public StyleNotFoundException(String styleName)
	{
		super(ERROR_CODE);
		this.styleName = requireNonNull(styleName);
	}

	@Override
	public String getMessage()
	{
		return String.format("styleName:%s", styleName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, styleName);
		}).orElseGet(super::getLocalizedMessage);
	}
}

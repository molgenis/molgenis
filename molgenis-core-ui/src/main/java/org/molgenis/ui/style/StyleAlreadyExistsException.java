package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class StyleAlreadyExistsException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "C12";
	private String styleId;

	public StyleAlreadyExistsException(String styleId)
	{
		super(ERROR_CODE);
		this.styleId = requireNonNull(styleId);
	}

	@Override
	public String getMessage()
	{
		return String.format("styleId:%s", styleId);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, styleId);
		}).orElseGet(super::getLocalizedMessage);
	}
}

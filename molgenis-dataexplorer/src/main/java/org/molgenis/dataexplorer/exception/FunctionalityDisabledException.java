package org.molgenis.dataexplorer.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class FunctionalityDisabledException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G01";
	private String functionality;

	public FunctionalityDisabledException(String functionality)
	{
		super(ERROR_CODE);
		this.functionality = requireNonNull(functionality);
	}

	public String getFunctionality()
	{
		return functionality;
	}

	@Override
	public String getMessage()
	{
		return String.format("Funcionality:%s", functionality);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, functionality);
		}).orElseGet(super::getLocalizedMessage);
	}
}

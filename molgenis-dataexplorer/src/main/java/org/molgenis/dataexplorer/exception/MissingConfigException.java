package org.molgenis.dataexplorer.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingConfigException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DE03";
	private String configName;

	public MissingConfigException(String configName)
	{
		super(ERROR_CODE);
		this.configName = requireNonNull(configName);
	}

	public String getConfigName()
	{
		return configName;
	}

	@Override
	public String getMessage()
	{
		return String.format("config:%s", configName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, configName);
		}).orElseGet(super::getLocalizedMessage);
	}
}

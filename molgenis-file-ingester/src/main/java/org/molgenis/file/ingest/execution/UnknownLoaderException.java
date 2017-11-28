package org.molgenis.file.ingest.execution;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownLoaderException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "F01";
	private String loader;

	public UnknownLoaderException(String loader)
	{
		super(ERROR_CODE);
		this.loader = loader;
	}

	public String getLoader()
	{
		return loader;
	}

	@Override
	public String getMessage()
	{
		return String.format("loader:%s", loader);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, loader);
		}).orElse(super.getLocalizedMessage());
	}
}

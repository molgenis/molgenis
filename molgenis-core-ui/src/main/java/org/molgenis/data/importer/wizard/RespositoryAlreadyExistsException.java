package org.molgenis.data.importer.wizard;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class RespositoryAlreadyExistsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C07";
	private String name;

	public RespositoryAlreadyExistsException(String name)
	{
		super(ERROR_CODE);
		this.name = requireNonNull(name);
	}

	@Override
	public String getMessage()
	{
		return String.format("name:%s", name);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, name);
		}).orElse(super.getLocalizedMessage());
	}
}

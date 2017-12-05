package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnexpectedPedigreeInformationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF06";
	private String value;
	private String line;

	public UnexpectedPedigreeInformationException(String value, String line)
	{
		super(ERROR_CODE);
		this.value = requireNonNull(value);
		this.line = requireNonNull(line);
	}

	@Override
	public String getMessage()
	{
		return String.format("value:%s line:%s", value, line);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, value, line);
		}).orElse(super.getLocalizedMessage());
	}
}

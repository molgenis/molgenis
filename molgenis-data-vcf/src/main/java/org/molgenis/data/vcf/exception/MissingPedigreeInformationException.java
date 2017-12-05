package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingPedigreeInformationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF04";
	private String line;

	public MissingPedigreeInformationException(String line)
	{
		super(ERROR_CODE);
		this.line = requireNonNull(line);
	}

	@Override
	public String getMessage()
	{
		return String.format("line:%s", line);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, line);
		}).orElse(super.getLocalizedMessage());
	}
}

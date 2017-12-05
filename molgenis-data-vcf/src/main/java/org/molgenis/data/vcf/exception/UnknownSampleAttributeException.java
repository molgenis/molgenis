package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownSampleAttributeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF03";
	private String sampleAttribute;

	public UnknownSampleAttributeException(String sampleAttribute)
	{
		super(ERROR_CODE);
		this.sampleAttribute = requireNonNull(sampleAttribute);
	}

	@Override
	public String getMessage()
	{
		return String.format("sampleAttribute:%s", sampleAttribute);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, sampleAttribute);
		}).orElse(super.getLocalizedMessage());
	}
}

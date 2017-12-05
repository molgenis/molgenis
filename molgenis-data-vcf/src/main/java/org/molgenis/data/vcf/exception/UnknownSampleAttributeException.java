package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownSampleAttributeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF03";
	private Attribute attribute;

	public UnknownSampleAttributeException(Attribute attribute)
	{
		super(ERROR_CODE);
		this.attribute = requireNonNull(attribute);
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s", attribute.getName());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, attribute.getLabel(language));
		}).orElse(super.getLocalizedMessage());
	}
}

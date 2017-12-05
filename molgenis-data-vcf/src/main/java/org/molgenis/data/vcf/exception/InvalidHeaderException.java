package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidHeaderException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF07";

	public InvalidHeaderException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return String.format("");
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return format;
		}).orElse(super.getLocalizedMessage());
	}
}

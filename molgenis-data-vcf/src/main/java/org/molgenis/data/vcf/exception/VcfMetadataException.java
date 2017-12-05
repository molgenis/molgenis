package org.molgenis.data.vcf.exception;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class VcfMetadataException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "VCF02";

	public VcfMetadataException(Throwable cause)
	{
		super(ERROR_CODE, cause);
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

package org.molgenis.data.mapper.exception;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class IllegalTargetPackageException extends MappingServiceException
{
	private static final String ERROR_CODE = "M02";

	private final String packageId;

	public IllegalTargetPackageException(String packageId)
	{
		super(ERROR_CODE);
		this.packageId = requireNonNull(packageId);
	}

	public String getPackageId()
	{
		return packageId;
	}

	@Override
	public String getMessage()
	{
		return format("id:%s", packageId);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, packageId);
		}).orElse(super.getLocalizedMessage());
	}
}

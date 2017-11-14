package org.molgenis.data;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:S1948")
public class UnknownPackageException extends UnknownDataException
{
	private static final String ERROR_CODE = "D07";

	private final Object packageId;

	public UnknownPackageException(Object packageId)
	{
		super(ERROR_CODE);
		this.packageId = requireNonNull(packageId);
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", packageId.toString());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, packageId.toString());
		}).orElse(super.getLocalizedMessage());
	}
}


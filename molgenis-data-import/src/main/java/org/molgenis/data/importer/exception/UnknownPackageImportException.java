package org.molgenis.data.importer.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownPackageImportException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "I01";

	private final String entityId;
	private final String packageId;

	public UnknownPackageImportException(String packageId, String entityId)
	{
		super(ERROR_CODE);
		this.entityId = entityId;
		this.packageId = packageId;
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s entityId: %s", packageId.toString(), entityId.toString());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, packageId.toString(), entityId.toString());
		}).orElse(super.getLocalizedMessage());
	}
}

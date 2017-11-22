package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InconsistentPackageStructureException extends EmxException
{
	private final static String ERROR_CODE = "E01";
	private String package_;
	private String parent;

	public InconsistentPackageStructureException(String package_, String parent)
	{
		super(ERROR_CODE);
		this.package_ = package_;
		this.parent = parent;
	}

	@Override
	public String getMessage()
	{
		return String.format("package:%s parent:%s", package_, parent);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, package_, parent);
		}).orElse(super.getLocalizedMessage());
	}
}

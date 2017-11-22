package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownReferenceEntityException extends EmxException
{
	private final static String ERROR_CODE = "E12";
	private String attributeAttribute;
	private String attributeName;
	private String refEntityName;
	private int rowIndex;

	public UnknownReferenceEntityException(String attributeAttribute, String attributeName, String refEntityName,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.attributeName = attributeName;
		this.refEntityName = refEntityName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s referenceName:%s, rowIndex:%d", attributeName, refEntityName, rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, refEntityName, attributeName, rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}

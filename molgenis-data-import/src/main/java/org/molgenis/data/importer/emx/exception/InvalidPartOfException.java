package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidPartOfException extends EmxException
{
	private final static String ERROR_CODE = "E05";
	private String partOfAttributeAttribute;
	private String attributeName;
	private String entityTypeId;
	private int rowIndex;

	public InvalidPartOfException(String partOfAttributeAttribute, String attributeName, String entityTypeId,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.partOfAttributeAttribute = partOfAttributeAttribute;
		this.attributeName = attributeName;
		this.entityTypeId = entityTypeId;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("partOfAttributeAttribute:%s attribute:%s entityTypeId: %s rowIndex:%d",
				partOfAttributeAttribute, attributeName, entityTypeId, rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, partOfAttributeAttribute, attributeName, entityTypeId, rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}

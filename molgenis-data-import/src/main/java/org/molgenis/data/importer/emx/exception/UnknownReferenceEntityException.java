package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownReferenceEntityException extends EmxException
{
	private final static String ERROR_CODE = "E12";
	private String attributeAttribute;
	private Attribute attribute;
	private String refEntityName;
	private int rowIndex;

	public UnknownReferenceEntityException(String attributeAttribute, Attribute attribute, String refEntityName,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.attributeAttribute = attributeAttribute;
		this.attribute = attribute;
		this.refEntityName = refEntityName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s referenceName:%s, rowIndex:%d", attribute.getName(), refEntityName,
				rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, refEntityName, attribute.getName(), rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}

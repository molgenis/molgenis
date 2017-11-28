package org.molgenis.data.index.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class IndexAlreadyExistsException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "IDX01";
	private String indexName;

	public IndexAlreadyExistsException(String indexName)
	{
		super(ERROR_CODE);
		this.indexName = indexName;
	}

	public String getIndexName()
	{
		return indexName;
	}

	@Override
	public String getMessage()
	{
		return String.format("indexName:%s", indexName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, indexName);
		}).orElse(super.getLocalizedMessage());
	}
}

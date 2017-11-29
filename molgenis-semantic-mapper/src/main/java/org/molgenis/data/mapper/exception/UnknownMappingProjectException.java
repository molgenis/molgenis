package org.molgenis.data.mapper.exception;

import java.text.MessageFormat;

import static java.lang.String.format;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownMappingProjectException extends MappingServiceException
{
	private static final String ERROR_CODE = "M01";

	private final String mappingProjectId;

	public UnknownMappingProjectException(String mappingProjectId)
	{
		super(ERROR_CODE);
		this.mappingProjectId = mappingProjectId;
	}

	public String getMappingProjectId()
	{
		return mappingProjectId;
	}

	@Override
	public String getMessage()
	{
		return format("id:%s", mappingProjectId);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, mappingProjectId);
		}).orElseGet(super::getLocalizedMessage);
	}
}

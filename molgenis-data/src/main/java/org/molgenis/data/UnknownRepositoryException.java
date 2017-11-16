package org.molgenis.data;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownRepositoryException extends UnknownDataException
{
	private static final String ERROR_CODE = "D05";

	private final String repositoryId;

	public UnknownRepositoryException(String repositoryId)
	{
		super(ERROR_CODE);
		this.repositoryId = requireNonNull(repositoryId);
	}

	@Override
	public String getMessage()
	{
		return String.format("repository:%s", repositoryId);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(
				languageService -> MessageFormat.format(languageService.getString(ERROR_CODE), repositoryId))
								   .orElse(super.getLocalizedMessage());
	}
}

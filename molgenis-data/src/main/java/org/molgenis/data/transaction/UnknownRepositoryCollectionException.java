package org.molgenis.data.transaction;

import org.molgenis.data.UnknownDataException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownRepositoryCollectionException extends UnknownDataException
{
	private static final String ERROR_CODE = "D06";

	private final String repositoryCollectionId;

	public UnknownRepositoryCollectionException(String repositoryCollectionId)
	{
		super(ERROR_CODE);
		this.repositoryCollectionId = requireNonNull(repositoryCollectionId);
	}

	@Override
	public String getMessage()
	{
		return String.format("collection:%s", repositoryCollectionId);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(
				languageService -> MessageFormat.format(languageService.getString(ERROR_CODE), repositoryCollectionId))
								   .orElse(super.getLocalizedMessage());
	}
}


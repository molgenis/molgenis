package org.molgenis.data;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class RepositoryCollectionCapabilityException extends UnknownDataException
{
	private static final String ERROR_CODE = "D13";

	private final String repositoryCollectionId;
	private RepositoryCapability capability;

	public RepositoryCollectionCapabilityException(String repositoryCollectionId, RepositoryCapability capability)
	{
		super(ERROR_CODE);
		this.repositoryCollectionId = requireNonNull(repositoryCollectionId);
		this.capability = requireNonNull(capability);
	}

	@Override
	public String getMessage()
	{
		return String.format("collection:%s capability:%s", repositoryCollectionId, capability);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(
				languageService -> MessageFormat.format(languageService.getString(ERROR_CODE), repositoryCollectionId,
						capability)).orElse(super.getLocalizedMessage());
	}
}


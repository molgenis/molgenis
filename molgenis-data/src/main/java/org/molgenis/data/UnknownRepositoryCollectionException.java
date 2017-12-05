package org.molgenis.data;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
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
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { repositoryCollectionId };
	}
}


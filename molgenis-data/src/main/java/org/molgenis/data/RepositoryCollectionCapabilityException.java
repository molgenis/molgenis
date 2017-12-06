package org.molgenis.data;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RepositoryCollectionCapabilityException extends UnknownDataException
{
	private static final String ERROR_CODE = "D13";

	private final String repositoryCollectionId;
	private final RepositoryCapability capability;

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
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { repositoryCollectionId, capability };
	}
}


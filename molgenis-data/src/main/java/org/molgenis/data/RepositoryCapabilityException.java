package org.molgenis.data;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RepositoryCapabilityException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D09";

	private final transient Repository repository;
	private final RepositoryCapability repositoryCapability;

	public RepositoryCapabilityException(Repository repository, RepositoryCapability repositoryCapability)
	{
		super(ERROR_CODE);
		this.repository = requireNonNull(repository);
		this.repositoryCapability = requireNonNull(repositoryCapability);
	}

	@Override
	public String getMessage()
	{
		return String.format("repository:%s capability:%s", repository.getName(), repositoryCapability.name());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { repository.getEntityType(), repositoryCapability.toString() };
	}
}


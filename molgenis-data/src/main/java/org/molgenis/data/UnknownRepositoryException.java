package org.molgenis.data;

import static java.util.Objects.requireNonNull;

/**
 * @see Repository
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
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
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { repositoryId };
	}
}

package org.molgenis.data;

import static java.lang.String.format;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class UnknownRepositoryException extends MolgenisDataException
{
	public UnknownRepositoryException(String repoName)
	{
		super(format("Unknown repository [%s]", repoName));
	}

	public UnknownRepositoryException(String repoName, String repoCollectionName)
	{
		super(format("Unknown repository [%s] in repository collection [%s]", repoName, repoCollectionName));
	}
}

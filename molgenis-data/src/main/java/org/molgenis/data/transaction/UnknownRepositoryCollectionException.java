package org.molgenis.data.transaction;

import static java.lang.String.format;

import org.molgenis.data.MolgenisDataException;

public class UnknownRepositoryCollectionException extends MolgenisDataException
{
	public UnknownRepositoryCollectionException(String repoCollectionName)
	{
		super(format("Unknown repository collection [%s]", repoCollectionName));
	}
}

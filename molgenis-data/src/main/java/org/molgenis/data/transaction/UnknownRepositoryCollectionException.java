package org.molgenis.data.transaction;

import org.molgenis.data.MolgenisDataException;

import static java.lang.String.format;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class UnknownRepositoryCollectionException extends MolgenisDataException
{
	public UnknownRepositoryCollectionException(String repoCollectionName)
	{
		super(format("Unknown repository collection [%s]", repoCollectionName));
	}
}

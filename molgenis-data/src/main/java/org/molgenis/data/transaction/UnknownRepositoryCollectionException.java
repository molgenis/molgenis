package org.molgenis.data.transaction;

import org.molgenis.data.MolgenisDataException;

import static java.lang.String.format;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class UnknownRepositoryCollectionException extends MolgenisDataException
{
	public UnknownRepositoryCollectionException(String repoCollectionName)
	{
		super(format("Unknown repository collection [%s]", repoCollectionName));
	}
}

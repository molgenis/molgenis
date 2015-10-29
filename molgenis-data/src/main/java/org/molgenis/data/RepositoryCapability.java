package org.molgenis.data;

public enum RepositoryCapability
{
	WRITABLE, QUERYABLE, AGGREGATEABLE, UPDATEABLE,
	/**
	 * Repository backend can be created, repository backend can be dropped.
	 */
	MANAGABLE,
	/**
	 * Repository index can be rebuilt
	 */
	INDEXABLE
}

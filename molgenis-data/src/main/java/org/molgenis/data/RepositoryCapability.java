package org.molgenis.data;

public enum RepositoryCapability
{
	/**
	 * Repository entities can be updated/added/deleted
	 */
	WRITABLE,

	/**
	 * Queries can be executed against the repository
	 */
	QUERYABLE,

	/**
	 * Repository can return aggregated results
	 */
	AGGREGATEABLE,

	/**
	 * Repository backend can be created, repository backend can be dropped.
	 */
	MANAGABLE,

	/**
	 * Repository index can be rebuilt
	 */
	INDEXABLE
}

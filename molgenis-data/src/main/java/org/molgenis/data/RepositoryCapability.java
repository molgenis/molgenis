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
	INDEXABLE,

	/**
	 * Repository can validate references to entities persisted in other repositories in the same repository collection.
	 */
	VALIDATE_REFERENCE_CONSTRAINT,

	/**
	 * Repository can validate unique values for attributes
	 */
	VALIDATE_UNIQUE_CONSTRAINT,

	/**
	 * Repository can validate if values are not-null for attributes
	 */
	VALIDATE_NOTNULL_CONSTRAINT,

	/**
	 * Repository can validate if readonly values haven't changed
	 */
	VALIDATE_READONLY_CONSTRAINT,

	/**
	 * Repositoy can be cached in L1 and L2 cache
	 */
	CACHEABLE
}

package org.molgenis.data;

public enum RepositoryCollectionCapability
{
	/**
	 * Repository collections can be edited
	 */
	WRITABLE,

	/**
	 * Repository collections can be updated
	 */
	UPDATABLE,

	/**
	 * Repository collections persist meta data
	 */
	META_DATA_PERSISTABLE,

	/**
	 * Repository can store data in an L1, L2 or L3 cache
	 */
	CACHEABLE
}

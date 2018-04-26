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
	META_DATA_PERSISTABLE
}

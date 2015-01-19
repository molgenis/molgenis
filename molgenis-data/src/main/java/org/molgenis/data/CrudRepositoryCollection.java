package org.molgenis.data;

public interface CrudRepositoryCollection extends RepositoryCollection
{
	/**
	 * @return the name of this backend
	 */
	String getName();

	/**
	 * Gets a repository by name.
	 * 
	 * CrudRepositoryCollection contains crud repositories
	 * 
	 * @param name
	 * @return
	 */
	CrudRepository getCrudRepository(String name);

	/**
	 * Create and add a new CrudRepository for an EntityMetaData
	 */
	CrudRepository addEntityMeta(EntityMetaData entityMeta);
}

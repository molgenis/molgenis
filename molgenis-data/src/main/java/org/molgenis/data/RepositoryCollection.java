package org.molgenis.data;

/**
 * Repository collection
 */
public interface RepositoryCollection extends Iterable<Repository>
{
	/**
	 * @return the name of this backend
	 */
	String getName();

	/**
	 * Create and add a new CrudRepository for an EntityMetaData
	 */
	Repository addEntityMeta(EntityMetaData entityMeta);

	/**
	 * Get names of all the entities in this source
	 */
	Iterable<String> getEntityNames();

	/**
	 * Get a repository by entity name
	 * 
	 * @throws UnknownEntityException
	 */
	Repository getRepository(String name);
}

package org.molgenis.data;

/**
 * Repository collection
 */
public interface RepositoryCollection
{
	/**
	 * Get names of all the entities in this source
	 */
	Iterable<String> getEntityNames();

	// TODO remove
	/**
	 * Get a repository by entity name
	 * 
	 * @throws UnknownEntityException
	 */
	Repository getRepositoryByEntityName(String name);

	// TODO remove
	/**
	 * Get a repository by url
	 * 
	 * @param name
	 * @return Repository or null if not found
	 */
	Repository getRepositoryByUrl(String url);
}

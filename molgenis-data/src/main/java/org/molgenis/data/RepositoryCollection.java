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

	/**
	 * Get a repository by entity name
	 * 
	 * @throws UnknownEntityException
	 */
	Repository getRepositoryByEntityName(String name);

	/**
	 * Get a repository by url
	 * 
	 * @param name
	 * @return Repository or null if not found
	 */
	Repository getRepositoryByUrl(String url);
}

package org.molgenis.data;

/**
 * Interface for a backend that can create repositories.
 */
public interface RepositoryCreator
{
	/**
	 * Creates a CrudRepository and registers it with the DataService. The repository may already contain data upon
	 * creation if the backend is persistent and the repository has been created before.
	 * 
	 * @param emd
	 *            the metadata of the entities stored in the repository
	 * @return the created CrudRepository
	 */
	CrudRepository create(EntityMetaData emd);
}

package org.molgenis.data;

public interface CrudRepositoryCollection extends RepositoryCollection
{
	/**
	 * Gets a repository by name.
	 * 
	 * CrudRepositoryCollection contains crud repositories
	 * 
	 * @param name
	 * @return
	 */
	CrudRepository getCrudRepository(String name);
}

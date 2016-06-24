package org.molgenis.data;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
	 * Streams the {@link Repository}s
	 */
	default Stream<Repository> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}

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

	/**
	 * Check if a repository exists by entity name
	 *
	 */
	boolean hasRepository(String name);
}

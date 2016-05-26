package org.molgenis.data;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Repository collection
 */
public interface RepositoryCollection extends Iterable<Repository<Entity>>
{
	/**
	 * All back-ends names
	 * */
	public static enum BACKEND
	{
		PostgreSQL, ElasticSearch, IdCard, EXCEL, CSV;
	};

	/**
	 * @return the name of this backend
	 */
	String getName();

	/**
	 * Streams the {@link Repository}s
	 */
	default Stream<Repository<Entity>> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Create and add a new CrudRepository for an EntityMetaData
	 */
	Repository<Entity> addEntityMeta(EntityMetaData entityMeta);

	/**
	 * Get names of all the entities in this source
	 */
	Iterable<String> getEntityNames();

	/**
	 * Get a repository by entity name
	 * 
	 * @throws UnknownEntityException
	 */
	Repository<Entity> getRepository(String name);

	/**
	 * Check if a repository exists by entity name
	 *
	 */
	boolean hasRepository(String name);
}

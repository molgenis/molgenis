package org.molgenis.data;

import java.io.Closeable;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Repository gives access to a collection of Entity. Synonyms: EntityReader, EntitySource, EntityCollection
 */
public interface Repository extends Iterable<Entity>, Closeable
{
	/**
	 * Streams the {@link Entity}s
	 */
	default Stream<Entity> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Streams the {@link Entity}s
	 * 
	 * @param fetch
	 *            fetch defining which attributes to retrieve
	 * @return Stream of all entities
	 */
	Stream<Entity> stream(Fetch fetch);

	Set<RepositoryCapability> getCapabilities();

	String getName();

	EntityMetaData getEntityMetaData();

	long count();

	Query query();

	/**
	 * return number of entities matched by query
	 **/
	long count(Query q);

	/**
	 * Find entities that match a query. Returns empty stream if no matches.
	 * 
	 * @return (empty) Stream, never null
	 */
	Stream<Entity> findAll(Query q);

	/**
	 * Find an entity base on a query
	 * 
	 * Returns null if not exists.
	 * 
	 * Returns first result if multiple found
	 */
	Entity findOne(Query q);

	/**
	 * Type-safe find one entity based on id. Returns null if not exists
	 */
	Entity findOne(Object id);

	/**
	 * Find one entity based on id.
	 * 
	 * @param id
	 *            entity id
	 * @param fetch
	 *            fetch defining which attributes to retrieve
	 * @return entity or null
	 * @throws MolgenisDataAccessException
	 */
	Entity findOne(Object id, Fetch fetch);

	/**
	 * Finds all entities with the given IDs. Returns empty stream if no matches.
	 * 
	 * @param ids
	 *            entity ids
	 * 
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 */
	Stream<Entity> findAll(Stream<Object> ids);

	/**
	 * Finds all entities with the given IDs, with a fetch. Returns empty stream if no matches.
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param ids
	 *            entity ids
	 * @param fetch
	 *            fetch defining which attributes to retrieve
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 */
	Stream<Entity> findAll(Stream<Object> ids, Fetch fetch);

	/**
	 * 
	 * @param aggregateQuery
	 * @return
	 */
	AggregateResult aggregate(AggregateQuery aggregateQuery);

	/* Update one entity */
	void update(Entity entity);

	/**
	 * Updates the given entities
	 * 
	 * @param entities
	 */
	void update(Stream<? extends Entity> entities);

	/* Delete one entity */
	void delete(Entity entity);

	/**
	 * Delete entities from repository
	 * 
	 * @param entities
	 *            entity stream
	 */
	void delete(Stream<? extends Entity> entities);

	/* Delete one entity based on id */
	void deleteById(Object id);

	/* Streaming delete based on multiple ids */
	void deleteById(Stream<Object> ids);

	/* Delete all entities */
	void deleteAll();

	/** Add one entity */
	void add(Entity entity);

	/**
	 * Add entities to repisotory
	 * 
	 * @param entities
	 * @return number of added entities
	 */
	Integer add(Stream<? extends Entity> entities);

	void flush();

	void clearCache();

	/**
	 * Create a new repository backend (e.g., create a table in a database; add a sheet to Excel)
	 */
	public void create();

	/**
	 * Drop a repository backend (e.g. drop a table in a database; remove a sheet from Excel)
	 */
	public void drop();

	/**
	 * Rebuild current index
	 */
	public void rebuildIndex();

	/**
	 * Adds an entity listener for a entity that listens to entity changes
	 * 
	 * @param entityListener
	 *            entity listener for a entity
	 */
	void addEntityListener(EntityListener entityListener);

	/**
	 * Removes an entity listener
	 * 
	 * @param entityListener
	 *            entity listener for a entity
	 */
	void removeEntityListener(EntityListener entityListener);
}

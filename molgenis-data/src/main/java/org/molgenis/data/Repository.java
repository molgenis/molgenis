package org.molgenis.data;

import java.io.Closeable;
import java.util.Set;

/**
 * Repository gives access to a collection of Entity. Synonyms: EntityReader, EntitySource, EntityCollection
 */
public interface Repository extends Iterable<Entity>, Closeable
{
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
	 * type-safe find entities that match a query
	 * 
	 * @return (empty) Iterable, never null
	 */
	Iterable<Entity> findAll(Query q);

	/**
	 * Find an entity base on a query
	 * 
	 * Returns null if not exists.
	 * 
	 * Returns first result if multiple found
	 */
	Entity findOne(Query q);

	/**
	 * type-safe find one entity based on id. Returns null if not exists
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
	 * find entities based on a stream of ids.
	 * 
	 * @return (empty) Iterable where the order of entities matches the order of ids, never null
	 */
	Iterable<Entity> findAll(Iterable<Object> ids);

	/**
	 * Find entities based on id.
	 * 
	 * @param ids
	 *            entity ids
	 * @param fetch
	 *            fetch defining which attributes to retrieve
	 * @return (empty) Iterable where the order of entities matches the order of ids, never null
	 * @throws MolgenisDataAccessException
	 */
	Iterable<Entity> findAll(Iterable<Object> ids, Fetch fetch);

	/**
	 * 
	 * @param aggregateQuery
	 * @return
	 */
	AggregateResult aggregate(AggregateQuery aggregateQuery);

	/* Update one entity */
	void update(Entity entity);

	/* Streaming update multiple entities */
	void update(Iterable<? extends Entity> records);

	/* Delete one entity */
	void delete(Entity entity);

	/* Streaming delete multiple entities */
	void delete(Iterable<? extends Entity> entities);

	/* Delete one entity based on id */
	void deleteById(Object id);

	/* Streaming delete based on multiple ids */
	void deleteById(Iterable<Object> ids);

	/* Delete all entities */
	void deleteAll();

	/** Add one entity */
	void add(Entity entity);

	/** Stream add multiple entities */
	Integer add(Iterable<? extends Entity> entities);

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

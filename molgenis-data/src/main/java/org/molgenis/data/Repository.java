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
	 * find entities based on a stream of ids.
	 * 
	 * @return (empty) Iterable where the order of entities matches the order of ids, never null
	 */
	Iterable<Entity> findAll(Iterable<Object> ids);

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
}

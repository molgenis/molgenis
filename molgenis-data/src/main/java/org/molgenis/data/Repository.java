package org.molgenis.data;

import java.io.Closeable;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.meta.EntityMetaData;

/**
 * Repository gives access to a collection of Entity. Synonyms: EntityReader, EntitySource, EntityCollection
 */
public interface Repository<E extends Entity> extends Iterable<E>, Closeable
{
	/**
	 * Streams the {@link Entity}s
	 */
	default Stream<E> stream()
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
	Stream<E> stream(Fetch fetch);

	Set<RepositoryCapability> getCapabilities();

	String getName();

	EntityMetaData getEntityMetaData();

	long count();

	Query<E> query();

	/**
	 * return number of entities matched by query
	 *
	 * @param q*/
	long count(Query<E> q);

	/**
	 * Find entities that match a query. Returns empty stream if no matches.
	 * 
	 * @return (empty) Stream, never null
	 * @param q
	 */
	Stream<E> findAll(Query<E> q);

	/**
	 * Find an entity base on a query
	 * 
	 * Returns null if not exists.
	 * 
	 * Returns first result if multiple found
	 * @param q
	 */
	E findOne(Query<E> q);

	/**
	 * Type-safe find one entity based on id. Returns null if not exists
	 */
	E findOneById(Object id);

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
	E findOneById(Object id, Fetch fetch);

	/**
	 * Finds all entities with the given IDs. Returns empty stream if no matches.
	 * 
	 * @param ids
	 *            entity ids
	 *
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 */
	Stream<E> findAll(Stream<Object> ids);

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
	Stream<E> findAll(Stream<Object> ids, Fetch fetch);

	/**
	 * 
	 * @param aggregateQuery
	 * @return
	 */
	AggregateResult aggregate(AggregateQuery aggregateQuery);

	/* Update one entity */
	void update(E entity);

	/**
	 * Updates the given entities
	 *
	 * @param entities
	 */
	void update(Stream<E> entities);

	/* Delete one entity */
	void delete(E entity);

	/**
	 * Delete entities from repository
	 *
	 * @param entities
	 *            entity stream
	 */
	void delete(Stream<E> entities);

	/* Delete one entity based on id */
	void deleteById(Object id);

	/* Streaming delete based on multiple ids */
	void deleteAll(Stream<Object> ids);

	/* Delete all entities */
	void deleteAll();

	/** Add one entity
	 * @param entity*/
	void add(E entity);

	/**
	 * Add entities to repisotory
	 * 
	 * @param entities
	 * @return number of added entities
	 */
	Integer add(Stream<E> entities);

	void flush();

	void clearCache();

	/**
	 * Rebuild current index
	 * 
	 * TODO move to RepositoryCollection
	 */
	void rebuildIndex();

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

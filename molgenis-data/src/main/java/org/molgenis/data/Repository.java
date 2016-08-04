package org.molgenis.data;

import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.EntityMetaData;

import java.io.Closeable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Repository gives access to a collection of Entity. Synonyms: EntityReader, EntitySource, EntityCollection
 */
public interface Repository<E extends Entity> extends Iterable<E>, Closeable
{
	/**
	 * Executes a function for each batch of entities.
	 *
	 * @param consumer  function to call for each batch of entities
	 * @param batchSize size of the batches to feed to the consumer
	 */
	default void forEachBatched(Consumer<List<E>> consumer, int batchSize)
	{
		forEachBatched(null, consumer, batchSize);
	}

	/**
	 * Executes a function for each batch of entities.
	 *
	 * @param fetch     fetch defining which attributes to retrieve
	 * @param consumer  function to call for each batch of entities
	 * @param batchSize size of the batches to feed to the consumer
	 */
	void forEachBatched(Fetch fetch, Consumer<List<E>> consumer, int batchSize);

	/**
	 * Returns repository capabilities, e.g. writable
	 *
	 * @return all capabilities of this repository
	 */
	Set<RepositoryCapability> getCapabilities();

	/**
	 * Returns query operators supported by this repository, e.g. equals
	 *
	 * @return all query operators supported by this repository
	 */
	Set<Operator> getQueryOperators();

	/**
	 * Returns the repository name
	 *
	 * @return repository name
	 */
	String getName();

	/**
	 * Returns the repository meta data
	 *
	 * @return repository meta data
	 */
	EntityMetaData getEntityMetaData();

	/**
	 * Returns the number of entities in this repository
	 *
	 * @return the number of entities in this repository
	 */
	long count();

	/**
	 * Returns a new query used to retrieve data from this repository
	 *
	 * @return repository query
	 */
	Query<E> query();

	/**
	 * return number of entities matched by query
	 *
	 * @param q query
	 */
	long count(Query<E> q);

	/**
	 * Find entities that match a query. Returns empty stream if no matches.
	 *
	 * @param q query
	 * @return (empty) Stream, never null
	 */
	Stream<E> findAll(Query<E> q);

	/**
	 * Find an entity base on a query
	 * <p>
	 * Returns null if not exists.
	 * <p>
	 * Returns first result if multiple found
	 *
	 * @param q query
	 */
	E findOne(Query<E> q);

	/**
	 * Type-safe find one entity based on id. Returns null if not exists
	 */
	E findOneById(Object id);

	/**
	 * Find one entity based on id.
	 *
	 * @param id    entity id
	 * @param fetch fetch defining which attributes to retrieve
	 * @return entity or null
	 * @throws MolgenisDataAccessException if access to entity with the given id is not allowed
	 */
	E findOneById(Object id, Fetch fetch);

	/**
	 * Finds all entities with the given IDs. Returns empty stream if no matches.
	 *
	 * @param ids entity ids
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 */
	Stream<E> findAll(Stream<Object> ids);

	/**
	 * Finds all entities with the given IDs, with a fetch. Returns empty stream if no matches.
	 *
	 * @param ids   entity ids
	 * @param fetch fetch defining which attributes to retrieve
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 * @throws MolgenisDataAccessException if access to an entity one of the given IDs is not allowed
	 */
	Stream<E> findAll(Stream<Object> ids, Fetch fetch);

	/**
	 * @param aggregateQuery aggregation query
	 * @return aggregated values for the given query
	 */
	AggregateResult aggregate(AggregateQuery aggregateQuery);

	/**
	 * Update one entity
	 *
	 * @param entity entity to update
	 */
	void update(E entity);

	/**
	 * Updates the given entities
	 *
	 * @param entities entity stream
	 */
	void update(Stream<E> entities);

	/**
	 * Delete one entity
	 *
	 * @param entity entity to delete
	 */
	void delete(E entity);

	/**
	 * Delete entities from repository
	 *
	 * @param entities entity stream
	 */
	void delete(Stream<E> entities);

	/* Delete one entity based on id */

	/**
	 * Delete one entity by id
	 *
	 * @param id entity id
	 */
	void deleteById(Object id);

	/**
	 * Delete one entities by id
	 *
	 * @param ids entity ids
	 */
	void deleteAll(Stream<Object> ids);

	/**
	 * Delete all entities
	 */
	void deleteAll();

	/**
	 * Add one entity
	 *
	 * @param entity entity to add
	 */
	void add(E entity);

	/**
	 * Add entities to repository
	 *
	 * @param entities entity stream
	 * @return number of added entities
	 */
	Integer add(Stream<E> entities);
}

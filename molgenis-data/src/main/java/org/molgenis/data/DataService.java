package org.molgenis.data;

import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;

import java.util.Set;
import java.util.stream.Stream;

/**
 * DataService is a facade that manages data sources Entity names should be unique over all data sources.
 * <p>
 * Main entry point for the DataApi
 */
public interface DataService extends Iterable<Repository<Entity>>
{
	void setMetaDataService(MetaDataService metaDataService);

	/**
	 * Get the MetaDataService
	 *
	 * @return meta data service
	 */
	MetaDataService getMeta();

	/**
	 * Get the capabilities of a repository
	 *
	 * @param repositoryName repository name
	 * @return repository capabilities
	 */
	Set<RepositoryCapability> getCapabilities(String repositoryName);

	/**
	 * check if a repository for this entity already exists
	 */
	boolean hasRepository(String entityTypeId);

	/**
	 * Get a CrudRepository by entity name
	 *
	 * @throws UnknownEntityException      when the repository can not be found
	 * @throws MolgenisDataException       if the repository doesn't implement CrudRepository
	 * @throws MolgenisDataAccessException
	 */
	Repository<Entity> getRepository(String entityTypeId);

	/**
	 * Returns a {@link Repository} for the given entity of the given type.
	 *
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed entity {@link Repository}
	 */
	<E extends Entity> Repository<E> getRepository(String entityTypeId, Class<E> entityClass);

	/**
	 * Returns the type for the given entity
	 *
	 * @return entity type
	 * @throws UnknownEntityException
	 */
	EntityType getEntityType(String entityTypeId);

	/**
	 * Returns the number of entities of the given type.
	 *
	 * @param entityTypeId entity name
	 * @return number of entities
	 */
	long count(String entityTypeId);

	/**
	 * return number of entities matched by query
	 *
	 * @throws MolgenisDataException       if the repository of the entity isn't a Queryable
	 * @throws MolgenisDataAccessException
	 */
	long count(String entityTypeId, Query<Entity> q);

	/**
	 * Find all entities of the given type. Returns empty Stream if no matches.
	 *
	 * @throws MolgenisDataException       if the repository of the entity isn't a Queryable
	 * @throws MolgenisDataAccessException
	 */
	Stream<Entity> findAll(String entityTypeId);

	/**
	 * type-safe find all entities
	 *
	 * @throws MolgenisDataAccessException
	 */
	<E extends Entity> Stream<E> findAll(String entityTypeId, Class<E> clazz);

	/**
	 * Find entities that match a query. Returns empty stream if no matches.
	 * <p>
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 *
	 * @throws MolgenisDataAccessException
	 */
	Stream<Entity> findAll(String entityTypeId, Query<Entity> q);

	/**
	 * Type-safe find entities that match a query
	 *
	 * @param q     query
	 * @param clazz entity class
	 * @throws MolgenisDataAccessException
	 */
	<E extends Entity> Stream<E> findAll(String entityTypeId, Query<E> q, Class<E> clazz);

	/**
	 * Finds all entities with the given IDs. Returns empty stream if no matches.
	 *
	 * @param ids entity ids
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 * @throws MolgenisDataAccessException
	 */
	Stream<Entity> findAll(String entityTypeId, Stream<Object> ids);

	/**
	 * Finds all entities with the given IDs, type-safely. Returns empty stream if no matches.
	 *
	 * @param entityTypeId entity name (case insensitive)
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 * @throws MolgenisDataAccessException
	 */
	<E extends Entity> Stream<E> findAll(String entityTypeId, Stream<Object> ids, Class<E> clazz);

	/**
	 * Finds all entities with the given IDs, with a fetch. Returns empty stream if no matches.
	 *
	 * @param entityTypeId entity name (case insensitive)
	 * @param ids          entity ids
	 * @param fetch        fetch defining which attributes to retrieve
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 * @throws MolgenisDataAccessException
	 */
	Stream<Entity> findAll(String entityTypeId, Stream<Object> ids, Fetch fetch);

	/**
	 * Finds all entities with the given IDs, type-safely and with a fetch. Returns empty stream if no matches.
	 *
	 * @param ids   entity ids
	 * @param fetch fetch defining which attributes to retrieve
	 * @param clazz typed entity class
	 * @return (empty) Stream of entities of the give type where the order of entities matches the order of ids, never
	 * null
	 * @throws MolgenisDataAccessException
	 */
	<E extends Entity> Stream<E> findAll(String entityTypeId, Stream<Object> ids, Fetch fetch, Class<E> clazz);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * <p>
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 *
	 * @throws MolgenisDataAccessException
	 */
	Entity findOneById(String entityTypeId, Object id);

	/**
	 * @param id    entity id
	 * @param clazz entity type
	 * @return typed entity
	 * @throws MolgenisDataAccessException
	 */
	<E extends Entity> E findOneById(String entityTypeId, Object id, Class<E> clazz);

	/**
	 * Find one entity based on id.
	 *
	 * @param id    entity id
	 * @param fetch fetch defining which attributes to retrieve
	 * @return entity or null
	 * @throws MolgenisDataAccessException
	 */
	Entity findOneById(String entityTypeId, Object id, Fetch fetch);

	/**
	 * Type-safe find one entity based on id.
	 *
	 * @param id    entity id
	 * @param fetch fetch defining which attributes to retrieve
	 * @param clazz typed entity class
	 * @return entity of the given type or null
	 * @throws MolgenisDataAccessException
	 */
	<E extends Entity> E findOneById(String entityTypeId, Object id, Fetch fetch, Class<E> clazz);

	/**
	 * Find one entity based on id. Returns null if not exists
	 *
	 * @throws MolgenisDataException       if the repository of the entity isn't a Queryable
	 * @throws MolgenisDataAccessException
	 */
	Entity findOne(String entityTypeId, Query<Entity> q);

	/**
	 * type-save find an entity by it's id
	 *
	 * @param q query
	 * @throws MolgenisDataAccessException
	 */
	<E extends Entity> E findOne(String entityTypeId, Query<E> q, Class<E> clazz);

	/**
	 * Adds an entity to it's repository
	 *
	 * @throws MolgenisDataException       if the repository of the entity isn't a Writable
	 * @throws MolgenisDataAccessException
	 */
	void add(String entityTypeId, Entity entity);

	/**
	 * Adds entities to it's repository
	 *
	 * @param entities entities
	 */
	<E extends Entity> void add(String entityTypeId, Stream<E> entities);

	/**
	 * Updates an entity
	 *
	 * @throws MolgenisDataException       if the repository of the entity isn't an Updateable
	 * @throws MolgenisDataAccessException
	 */
	void update(String entityTypeId, Entity entity);

	/**
	 * Updates entities
	 *
	 * @param entities entities
	 */
	<E extends Entity> void update(String entityTypeId, Stream<E> entities);

	/**
	 * Deletes an entity
	 *
	 * @throws MolgenisDataException       if the repository of the entity isn't an Updateable
	 * @throws MolgenisDataAccessException
	 */
	void delete(String entityTypeId, Entity entity);

	/**
	 * Delete entities from it's repository
	 *
	 * @param entities entities
	 */
	<E extends Entity> void delete(String entityTypeId, Stream<E> entities);

	/**
	 * Deletes an entity by it's id
	 *
	 * @param id entity id
	 * @throws MolgenisDataAccessException
	 */
	void deleteById(String entityTypeId, Object id);

	/**
	 * Deletes entities by id
	 *
	 * @param ids entity ids
	 */
	void deleteAll(String entityTypeId, Stream<Object> ids);

	/**
	 * Deletes all entities
	 */
	void deleteAll(String entityTypeId);

	/**
	 * Returns an untyped query
	 *
	 * @param entityTypeId entity name
	 * @return an untyped query
	 */
	Query<Entity> query(String entityTypeId);

	/**
	 * Returns a typed query
	 *
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return a typed query
	 */
	<E extends Entity> Query<E> query(String entityTypeId, Class<E> entityClass);

	/**
	 * Creates counts off all possible combinations of xAttr and yAttr attributes of an entity
	 *
	 * @param aggregateQuery aggregation query
	 * @return aggregation results
	 */
	AggregateResult aggregate(String entityTypeId, AggregateQuery aggregateQuery);

	/**
	 * Get identifiers of all entity types in this source
	 */
	Stream<String> getEntityTypeIds();
}

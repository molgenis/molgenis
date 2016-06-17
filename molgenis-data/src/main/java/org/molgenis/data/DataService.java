package org.molgenis.data;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.meta.MetaDataService;

/**
 * DataService is a façade that manages data sources Entity names should be unique over all data sources.
 * 
 * Main entry point for the DataApi
 */
public interface DataService extends Iterable<Repository<Entity>>
{
	void setMeta(MetaDataService metaDataService);

	/**
	 * Get the MetaDataService
	 * 
	 * @return
	 */
	MetaDataService getMeta();

	/**
	 * Get the capabilities of a repository
	 * 
	 * @param repositoryName
	 * @return
	 */
	Set<RepositoryCapability> getCapabilities(String repositoryName);

	/**
	 * check if a repository for this entity already exists
	 * 
	 * @param entityName
	 */
	boolean hasRepository(String entityName);

	/**
	 * Get a CrudRepository by entity name
	 * 
	 * @throws UnknownEntityException
	 *             when the repository can not be found
	 * 
	 * @throws MolgenisDataException
	 *             if the repository doesn't implement CrudRepository
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	Repository<Entity> getRepository(String entityName);

	/**
	 * Returns a {@link Repository} for the given entity of the given type.
	 *
	 * @param entityName  entity name
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed entity {@link Repository}
	 */
	<E extends Entity> Repository<E> getRepository(String entityName, Class<E> entityClass);

	/**
	 * Returns the meta data for the given entity
	 * 
	 * @throws UnknownEntityException
	 * @param entityName
	 * @return
	 */
	EntityMetaData getEntityMetaData(String entityName);

	/**
	 * return number of entities matched by query
	 * 
	 * @throws MolgenisDataException
	 *             if the repository of the entity isn't a Queryable
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	long count(String entityName, Query<Entity> q);

	/**
	 * Find all entities of the given type. Returns empty Stream if no matches.
	 * 
	 * @throws MolgenisDataException
	 *             if the repository of the entity isn't a Queryable
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	Stream<Entity> findAll(String entityName);

	/**
	 * type-safe find all entities
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	<E extends Entity> Stream<E> findAll(String entityName, Class<E> clazz);

	/**
	 * Find entities that match a query. Returns empty stream if no matches.
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 * 
	 * @throws MolgenisDataAccessException
	 *
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	Stream<Entity> findAll(String entityName, Query<Entity> q);

	/**
	 * Type-safe find entities that match a query
	 * 
	 * @throws MolgenisDataAccessException
	 *@param entityName
	 * @param q
	 */
	<E extends Entity> Stream<E> findAll(String entityName, Query<E> q, Class<E> clazz);

	/**
	 * Finds all entities with the given IDs. Returns empty stream if no matches.
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param ids
	 *            entity ids
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 */
	Stream<Entity> findAll(String entityName, Stream<Object> ids);

	/**
	 * Finds all entities with the given IDs, type-safely. Returns empty stream if no matches.
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 */
	<E extends Entity> Stream<E> findAll(String entityName, Stream<Object> ids, Class<E> clazz);

	/**
	 * Finds all entities with the given IDs, with a fetch. Returns empty stream if no matches.
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param ids
	 *            entity ids
	 * @param fetch
	 *            fetch defining which attributes to retrieve
	 * @return (empty) Stream where the order of entities matches the order of ids, never null
	 */
	Stream<Entity> findAll(String entityName, Stream<Object> ids, Fetch fetch);

	/**
	 * Finds all entities with the given IDs, type-safely and with a fetch. Returns empty stream if no matches.
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param ids
	 *            entity ids
	 * @param fetch
	 *            fetch defining which attributes to retrieve
	 * @param clazz
	 *            typed entity class
	 * @return (empty) Stream of entities of the give type where the order of entities matches the order of ids, never
	 *         null
	 */
	<E extends Entity> Stream<E> findAll(String entityName, Stream<Object> ids, Fetch fetch, Class<E> clazz);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	Entity findOneById(String entityName, Object id);

	/**
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param id
	 * @param clazz
	 * @return
	 */
	<E extends Entity> E findOneById(String entityName, Object id, Class<E> clazz);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */

	/**
	 * Find one entity based on id.
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param id
	 *            entity id
	 * @param fetch
	 *            fetch defining which attributes to retrieve
	 * @return entity or null
	 * @throws MolgenisDataAccessException
	 */
	Entity findOneById(String entityName, Object id, Fetch fetch);

	/**
	 * Type-safe find one entity based on id.
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param id
	 *            entity id
	 * @param fetch
	 *            fetch defining which attributes to retrieve
	 * @param clazz
	 *            typed entity class
	 * @return entity of the given type or null
	 * @throws MolgenisDataAccessException
	 */
	<E extends Entity> E findOneById(String entityName, Object id, Fetch fetch, Class<E> clazz);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * 
	 * @throws MolgenisDataException
	 *             if the repository of the entity isn't a Queryable
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	Entity findOne(String entityName, Query<Entity> q);

	/**
	 * type-save find an entity by it's id
	 * 
	 * @throws MolgenisDataAccessException
	 *@param entityName
	 * @param q
	 */
	<E extends Entity> E findOne(String entityName, Query<E> q, Class<E> clazz);

	/**
	 * Adds an entity to it's repository
	 * 
	 * @throws MolgenisDataException
	 *             if the repository of the entity isn't a Writable
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @return the id of the entity
	 */
	void add(String entityName, Entity entity);

	/**
	 * Adds entities to it's repository
	 *  @param entityName
	 *            entity name (case insensitive)
	 * @param entities
	 */
	<E extends Entity> void add(String entityName, Stream<E> entities);

	/**
	 * Updates an entity
	 * 
	 * @throws MolgenisDataException
	 *             if the repository of the entity isn't an Updateable
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	void update(String entityName, Entity entity);

	/**
	 * Updates entities
	 *  @param entityName
	 *            entity name (case insensitive)
	 * @param entities
	 */
	<E extends Entity> void update(String entityName, Stream<E> entities);

	/**
	 * Deletes an entity
	 * 
	 * @throws MolgenisDataException
	 *             if the repository of the entity isn't an Updateable
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	void delete(String entityName, Entity entity);

	/**
	 * Delete entities from it's repository
	 *  @param entityName
	 *            entity name (case insensitive)
	 * @param entities
	 */
	<E extends Entity> void delete(String entityName, Stream<E> entities);

	/**
	 * Deletes an entity by it's id
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param id
	 */
	void deleteById(String entityName, Object id);

	/**
	 * Deletes all entities
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	void deleteAll(String entityName);

	/**
	 * Returns an untyped query
	 *
	 * @param entityName entity name
	 * @return an untyped query
	 */
	Query<Entity> query(String entityName);

	/**
	 * Returns a typed query
	 *
	 * @param entityName  entity name
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return a typed query
	 */
	<E extends Entity> Query<E> query(String entityName, Class<E> entityClass);

	/**
	 * Creates counts off all possible combinations of xAttr and yAttr attributes of an entity
	 * 
	 * @param aggregateQuery
	 * @return
	 */
	AggregateResult aggregate(String entityName, AggregateQuery aggregateQuery);

	/**
	 * Get names of all the entities in this source
	 */
	Stream<String> getEntityNames();

	/**
	 * Adds an entity listener for a entity of the given class that listens to entity changes
	 * 
	 * @param entityListener
	 *            entity listener for a entity
	 */
	void addEntityListener(String entityName, EntityListener entityListener);

	/**
	 * Removes an entity listener for a entity of the given class
	 * 
	 * @param entityListener
	 *            entity listener for a entity
	 */
	void removeEntityListener(String entityName, EntityListener entityListener);

	Repository<Entity> copyRepository(Repository<Entity> repository, String newRepositoryId, String newRepositoryLabel);

	Repository<Entity> copyRepository(Repository<Entity> repository, String newRepositoryId, String newRepositoryLabel, Query<Entity> query);
}

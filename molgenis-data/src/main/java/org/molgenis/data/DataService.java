package org.molgenis.data;

import java.util.List;

/**
 * DataService is a façade that manages data sources Entity names should be unique over all data sources.
 * 
 * Main entry point for the DataApi
 */
public interface DataService extends RepositoryCollection
{
	/**
	 * Add a repository to the DataService
	 * 
	 * @throws MolgenisDataException
	 *             if entity name is already registered
	 * @param repository
	 */
	void addRepository(Repository repository);

	/**
	 * Remove a repository from the DataService
	 * 
	 * @throws MolgenisDataException
	 *             if repository/entity name is null
	 * 
	 * @throws MolgenisDataException
	 *             if repository/entity name doesn't exists
	 * @param repositoryName
	 */
	void removeRepository(String repositoryName);

	/**
	 * check ia a repository for this entity already exists
	 * 
	 * @param entityName
	 */
	boolean hasRepository(String entityName);

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
	long count(String entityName, Query q);

	/**
	 * Find all entities of the given type. Returns empty Iterable if no matches.
	 * 
	 * @throws MolgenisDataException
	 *             if the repository of the entity isn't a Queryable
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	Iterable<Entity> findAll(String entityName);

	/**
	 * Find entities that match a query. Returns empty Iterable if no matches.
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	Iterable<Entity> findAll(String entityName, Query q);

	/**
	 * Find entities based on id. Returns empty Iterable if no matches.
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param ids
	 * @return
	 */
	Iterable<Entity> findAll(String entityName, Iterable<Object> ids);

	@Deprecated
	/**
	 * Find entities that match a query. Returns empty List if no matches.
	 * Use Iterable<E> findAll instead or count(String entityName, Query q);
	 * 
	 * @param entityName entity name (case insensitive)
	 * @param q
	 * @return
	 */
	<E extends Entity> List<E> findAllAsList(String entityName, Query q);

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
	Entity findOne(String entityName, Object id);

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
	Entity findOne(String entityName, Query q);

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
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param entities
	 */
	void add(String entityName, Iterable<? extends Entity> entities);

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
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param entities
	 */
	void update(String entityName, Iterable<? extends Entity> entities);

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
	 * Deletes entities
	 * 
	 * @throws MolgenisDataException
	 *             if the repository of the entity isn't an Updateable
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	void delete(String entityName, Iterable<? extends Entity> entity);

	/**
	 * Deletes an entity by it's id
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 * @param id
	 */
	void delete(String entityName, Object id);

	/**
	 * Deletes all entities
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	void deleteAll(String entityName);

	/**
	 * Drops the repository for the given entity (but does not remove the repository)
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	void drop(String entityName);

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
	CrudRepository getCrudRepository(String entityName);

	Writable getWritableRepository(String entityName);

	Queryable getQueryableRepository(String entityName);

	Manageable getManageableRepository(String entityName);

	Query query(String entityName);

	// TODO remove
	/**
	 * Returns all entity classes. Returns empty Iterable if no entity classes.
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @return
	 */
	Iterable<Class<? extends Entity>> getEntityClasses();

	/**
	 * type-safe find entities that match a query
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	<E extends Entity> Iterable<E> findAll(String entityName, Query q, Class<E> clazz);

	/**
	 * type-safe find all entities
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	<E extends Entity> Iterable<E> findAll(String entityName, Class<E> clazz);

	/**
	 * type-safe find entities that match a stream of ids
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	<E extends Entity> Iterable<E> findAll(String entityName, Iterable<Object> ids, Class<E> clazz);

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
	<E extends Entity> E findOne(String entityName, Object id, Class<E> clazz);

	/**
	 * type-save find an entity by it's id
	 * 
	 * @throws MolgenisDataAccessException
	 * 
	 * @param entityName
	 *            entity name (case insensitive)
	 */
	<E extends Entity> E findOne(String entityName, Query q, Class<E> clazz);

	/**
	 * Creates counts off all possible combinations of xAttr and yAttr attributes of an entity
	 * 
	 * @param aggregateQuery
	 * @return
	 */
	AggregateResult aggregate(String entityName, AggregateQuery aggregateQuery);
}

package org.molgenis.data;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.molgenis.data.support.FileRepositorySource;

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
	 * Add all repositories of a RepositorySource
	 * 
	 * @param repositorySource
	 */
	void addRepositories(RepositorySource repositorySource);

	/**
	 * return number of entities matched by query
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	long count(String entityName, Query q);

	/**
	 * Find all entities of the given type. Returns empty Iterable if no matches.
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	Iterable<Entity> findAll(String entityName);

	/**
	 * Find entities that match a query. Returns empty Iterable if no matches.
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	Iterable<Entity> findAll(String entityName, Query q);

	/**
	 * Find entities based on id. Returns empty Iterable if no matches.
	 * 
	 * @param entityName
	 * @param ids
	 * @return
	 */
	Iterable<Entity> findAll(String entityName, Iterable<Integer> ids);

	@Deprecated
	/**
	 * Find entities that match a query. Returns empty List if no matches.
	 * Use Iterable<E> findAll instead or count(String entityName, Query q);
	 * 
	 * @param entityName
	 * @param q
	 * @return
	 */
	<E extends Entity> List<E> findAllAsList(String entityName, Query q);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	Entity findOne(String entityName, Integer id);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	Entity findOne(String entityName, Query q);

	/**
	 * Adds an entity to it's repository
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Writable
	 * 
	 * @return the id of the entity
	 */
	Integer add(String entityName, Entity entity);

	void add(String entityName, Iterable<? extends Entity> entities);

	/**
	 * Updates an entity
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't an Updateable
	 */
	void update(String entityName, Entity entity);

	void update(String entityName, Iterable<? extends Entity> entities);

	/**
	 * Deletes an entity
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't an Updateable
	 */
	void delete(String entityName, Entity entity);

	/**
	 * Deletes entities
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't an Updateable
	 */
	void delete(String entityName, Iterable<? extends Entity> entity);

	/**
	 * Deletes an entity by it's id
	 * 
	 * @param entityName
	 * @param id
	 */
	void delete(String entityName, int id);

	/**
	 * Get a CrudRepository by entity name
	 * 
	 * throws UnknownEntityException when the repository can not be found
	 * 
	 * throws MolgenisDataException if the repository doesn't implement CrudRepository
	 */
	CrudRepository getCrudRepository(String entityName);

	/**
	 * Returns all entity classes. Returns empty Iterable if no entity classes.
	 * 
	 * @return
	 */
	Iterable<Class<? extends Entity>> getEntityClasses();

	/**
	 * type-safe find entities that match a query
	 */
	<E extends Entity> Iterable<E> findAll(String entityName, Query q, Class<E> clazz);

	/**
	 * type-safe find all entities
	 */
	<E extends Entity> Iterable<E> findAll(String entityName, Class<E> clazz);

	/**
	 * type-safe find entities that match a stream of ids
	 */
	<E extends Entity> Iterable<E> findAll(String entityName, Iterable<Integer> ids, Class<E> clazz);

	<E extends Entity> E findOne(String entityName, Integer id, Class<E> clazz);

	/**
	 * type-save find an entity by it's id
	 */
	<E extends Entity> E findOne(String entityName, Query q, Class<E> clazz);

	/**
	 * Add a FileRepositorySource so it can be used by the 'createFileRepositySource' factory method
	 * 
	 * @param fileRepositorySource
	 */
	void addFileRepositorySourceClass(Class<? extends FileRepositorySource> clazz, Set<String> fileExtensions);

	/**
	 * Factory method for creating a new FileRepositorySource
	 * 
	 * For example an excel file
	 * 
	 * @param file
	 * @return
	 */
	FileRepositorySource createFileRepositorySource(File file);
}

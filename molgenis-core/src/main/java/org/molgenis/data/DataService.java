package org.molgenis.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * DataService is a façade that manages data sources Entity names should be unique over all data sources.
 * 
 * Main entry point for the DataApi
 */
public interface DataService extends RepositoryCollection, Iterable<EntitySource>
{
	/**
	 * Register a new EntitySource.
	 * 
	 * You should first register the EntitySourceFactory for this EntitySource
	 */
	void registerEntitySource(String url);

	/**
	 * return number of entities matched by query
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	long count(String entityName, Query q);

	/**
	 * Find entities that match a query
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	<E extends Entity> Iterable<E> findAll(String entityName, Query q);

	<E extends Entity> List<E> findAllAsList(String entityName, Query q);

	<E extends Entity> List<E> findAllAsList(String entityName, QueryRule... queryRules);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	<E extends Entity> E findOne(String entityName, Integer id);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	<E extends Entity> E findOne(String entityName, Query q);

	<E extends Entity> E findOne(String entityName, QueryRule... queryRules);

	/**
	 * Adds an entity to it's repository
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Writable
	 */
	<E extends Entity> void add(String entityName, E entity);

	/**
	 * Updates an entity
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't an Updateable
	 */
	<E extends Entity> void update(String entityName, E entity);

	/**
	 * Deletes an entity
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't an Updateable
	 */
	<E extends Entity> void delete(String entityName, E entity);

	/**
	 * Deletes entities
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't an Updateable
	 */
	<E extends Entity> void delete(String entityName, Iterable<E> entity);

	/**
	 * Get a CrudRepository by entity name
	 * 
	 * throws UnknownEntityException when the repository can not be found
	 * 
	 * throws MolgenisDataException if the repository doesn't implement CrudRepository
	 */
	<E extends Entity> CrudRepository<E> getCrudRepository(String entityName);

	/**
	 * Creates a new file based entity source (like excel, csv)
	 * 
	 * Does not register the EntitySource
	 */
	EntitySource createEntitySource(File file) throws IOException;
}

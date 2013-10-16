package org.molgenis.data;

import java.io.File;
import java.io.IOException;

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
	Iterable<? extends Entity> findAll(String entityName, Query q);

	/**
	 * Find one entity based on id. Returns null if not exists
	 * 
	 * throws MolgenisDataException if the repository of the entity isn't a Queryable
	 */
	Entity findOne(String entityName, Integer id);

	/**
	 * Creates a new file based entity source (like excel, csv)
	 * 
	 * Does not register the EntitySource
	 */
	EntitySource createEntitySource(File file) throws IOException;
}

package org.molgenis.data;

/**
 * DataService is a façade that manages data sources Entity names should be unique over all data sources.
 * 
 * Main entry point for the DataApi
 */
public interface DataService extends RepositoryCollection, Iterable<EntitySource>
{
	/**
	 * Register a new EntitySourceFactory of an EntitySource implementation
	 */
	void registerFactory(EntitySourceFactory entitySourceFactory);

	/**
	 * Register a new EntitySource.
	 * 
	 * You should first register the EntitySourceFactory for this EntitySource
	 */
	void registerEntitySource(String url);
}

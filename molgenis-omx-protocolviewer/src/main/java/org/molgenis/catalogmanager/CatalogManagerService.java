package org.molgenis.catalogmanager;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.study.UnknownStudyDefinitionException;

/**
 * Manage catalog releases
 * 
 * @author Dennis
 */
public interface CatalogManagerService
{
	/**
	 * Gets all available catalogs
	 * 
	 * @return Iterable of CatalogInfo
	 */
	Iterable<CatalogMeta> findCatalogs();

	/**
	 * Returns the catalog with the given id
	 * 
	 * @param id
	 * @return
	 * @throws UnknownCatalogException
	 */
	Catalog getCatalog(String id) throws UnknownCatalogException;

	/**
	 * Returns the catalog of the study definition with the given id
	 * 
	 * @param id
	 * @return
	 * @throws UnknownCatalogException
	 * @throws UnknownStudyDefinitionException
	 */
	Catalog getCatalogOfStudyDefinition(String id) throws UnknownCatalogException, UnknownStudyDefinitionException;

	/**
	 * Retrieves a catalog and store it in the database
	 * 
	 * @param id
	 * @throws UnknownCatalogException
	 */
	void loadCatalog(String id) throws UnknownCatalogException;

	/**
	 * Removes a catalog from the database
	 * 
	 * @param id
	 * @throws UnknownCatalogException
	 */
	void unloadCatalog(String id) throws UnknownCatalogException;

	/**
	 * Returns whether the catalog with the given id is loaded
	 * 
	 * @param id
	 * @return
	 * @throws UnknownCatalogException
	 */
	boolean isCatalogLoaded(String id) throws UnknownCatalogException;

	/**
	 * Retrieves a catalog of a study definition and store it in the database
	 * 
	 * @param id
	 * @throws UnknownCatalogException
	 * @throws UnknownStudyDefinitionException
	 */
	void loadCatalogOfStudyDefinition(String id) throws UnknownCatalogException, UnknownStudyDefinitionException;

	/**
	 * Retrieves a catalog of a study definition from the database
	 * 
	 * @param id
	 * @throws UnknownCatalogException
	 * @throws UnknownStudyDefinitionException
	 */
	void unloadCatalogOfStudyDefinition(String id) throws UnknownCatalogException, UnknownStudyDefinitionException;

	/**
	 * Returns whether the catalog for the study definition with the given id is loaded
	 * 
	 * @param id
	 * @return
	 * @throws UnknownCatalogException
	 * @throws UnknownStudyDefinitionException
	 */
	boolean isCatalogOfStudyDefinitionLoaded(String id) throws UnknownCatalogException, UnknownStudyDefinitionException;
}

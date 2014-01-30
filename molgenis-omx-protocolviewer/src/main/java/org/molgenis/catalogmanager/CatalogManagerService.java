package org.molgenis.catalogmanager;

import org.molgenis.catalog.CatalogService;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.study.UnknownStudyDefinitionException;

/**
 * Manage catalog releases
 * 
 * @author Dennis
 */
public interface CatalogManagerService extends CatalogService
{
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
}

package org.molgenis.lifelines.catalogue;

import java.util.List;

/**
 * Find and retrieve catalog releases
 * 
 * @author erwin
 * 
 */
public interface CatalogLoaderService
{
	/**
	 * Gets all available catalogs
	 * 
	 * @return List of CatalogInfo
	 */
	public List<CatalogInfo> findCatalogs();

	/**
	 * Retrieves a catalog and store it in the database
	 * 
	 * @param id
	 * @throws UnknownCatalogException
	 */
	public void loadCatalog(String id) throws UnknownCatalogException;

	/**
	 * Retrieves a catalog of a study definition and store it in the database
	 * 
	 * @param id
	 * @throws UnknownCatalogException
	 */
	public void loadCatalogOfStudyDefinition(String id) throws UnknownCatalogException;

}

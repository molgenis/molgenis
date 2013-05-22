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
	List<CatalogInfo> findCatalogs();

	/**
	 * Retrieves a catalog and store it in the database
	 * 
	 * @param id
	 */
	void loadCatalog(String id) throws UnknownCatalogException;

}

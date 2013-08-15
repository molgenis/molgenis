package org.molgenis.omx.catalog;

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
	 * Returns a preview of a catalog without storing it in the database
	 * 
	 * @param id
	 * @return
	 */
	public CatalogPreview getCatalogPreview(String id) throws UnknownCatalogException;

	/**
	 * Returns a preview of a catalog of a study definition without storing it in the database
	 * 
	 * @param id
	 * @return
	 */
	public CatalogPreview getCatalogOfStudyDefinitionPreview(String id) throws UnknownCatalogException;

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

package org.molgenis.catalog;

import org.molgenis.study.UnknownStudyDefinitionException;

public interface CatalogService {
    /**
     * Gets all available catalogs
     *
     * @return meta data for each catalog
     */
    Iterable<CatalogMeta> getCatalogs();

    /**
     * Get the catalog with the given id
     *
     * @param id catalog id
     * @return
     * @throws UnknownCatalogException
     */
    Catalog getCatalog(String id) throws UnknownCatalogException;

    /**
     * Returns whether the catalog with the given id is loaded
     *
     * @param id
     * @return
     * @throws UnknownCatalogException
     */
    boolean isCatalogLoaded(String id) throws UnknownCatalogException;

    /**
     * Get the catalog of the study definition with the given id
     *
     * @param id study definition id
     * @return
     * @throws UnknownCatalogException
     * @throws UnknownStudyDefinitionException
     */
    Catalog getCatalogOfStudyDefinition(String id) throws UnknownCatalogException, UnknownStudyDefinitionException;

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

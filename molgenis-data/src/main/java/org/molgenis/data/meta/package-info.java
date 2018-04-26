package org.molgenis.data.meta;

/**
 * These classes provide an implementation of {@link org.molgenis.data.meta.MetaDataService}
 * that can be used to administrate the metadata of the repositories.
 * Since the metadata structure is a graph, and graphs are rather awkward to work with when
 * stored in tables, the metadata beans are cached in memory.
 * <p>
 * The classes are internal implementation details and should only be accessed through
 * the {@link org.molgenis.data.meta.MetaDataServiceImpl} facade.
 */

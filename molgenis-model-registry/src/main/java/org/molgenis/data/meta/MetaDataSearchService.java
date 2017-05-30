package org.molgenis.data.meta;

import java.util.List;

public interface MetaDataSearchService
{
	/**
	 * Finds root packages based on a search term. Searches in all fields of package, entity and attribute meta data
	 *
	 * @param searchTerm
	 * @return
	 */
	List<PackageSearchResultItem> findRootPackages(String searchTerm);
}

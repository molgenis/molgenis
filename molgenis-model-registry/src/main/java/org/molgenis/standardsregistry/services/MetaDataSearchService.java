package org.molgenis.standardsregistry.services;

import org.molgenis.data.meta.model.Package;
import org.molgenis.standardsregistry.model.*;

import java.util.List;

public interface MetaDataSearchService
{

	/**
	 *
	 * <p>Search-method to search in model-registry.</p>
	 *
	 * @param packageSearchRequest
	 * @return {@link PackageSearchResponse}
	 */
	PackageSearchResponse search(PackageSearchRequest packageSearchRequest);

	/**
	 *
	 * <p>Return {@link StandardRegistryTag}s</p>
	 *
	 * @param {@link Package}
	 * @return {@link List< StandardRegistryTag >}
	 */
	List<StandardRegistryTag> getTagsForPackage(Package pkg);


	/**
	 *
	 *
	 *
	 * @param packageName
	 * @return {@List<StandardRegistryEntity>}
	 */
	List<StandardRegistryEntity> getEntitiesInPackage(String packageName);
}

package org.molgenis.standardsregistry.services;

import org.molgenis.data.meta.model.Package;
import org.molgenis.standardsregistry.model.PackageSearchRequest;
import org.molgenis.standardsregistry.model.PackageSearchResponse;
import org.molgenis.standardsregistry.model.StandardRegistryEntity;
import org.molgenis.standardsregistry.model.StandardRegistryTag;

import java.util.List;

/**
 * <p>Service to search for metaData in data library. </p>
 * <ul>It uses the following services:
 * <li>{@link org.molgenis.data.meta.MetaDataService}</li>
 * <li>{@link org.molgenis.data.DataService}</li>
 * <li>{@link org.molgenis.data.semanticsearch.service.TagService}</li>
 * <li>{@link org.molgenis.security.core.MolgenisPermissionService}</li>
 * </ul>
 *
 * @author sido
 */
public interface MetaDataSearchService
{

	/**
	 * <p>Search-method to search in model-registry.</p>
	 *
	 * @param query
	 * @param offSet
	 * @param number
	 *
	 * @return {@link PackageSearchResponse}
	 */
	PackageSearchResponse search(String query, int offSet, int number);

	/**
	 * <p>Return {@link StandardRegistryTag}-list</p>
	 *
	 * @param pkg {@link Package}
	 * @return {@link List< StandardRegistryTag >}
	 */
	List<StandardRegistryTag> getTagsForPackage(Package pkg);

	/**
	 * <p>Return {@link  StandardRegistryEntity}-list</p>
	 *
	 * @param packageName package-name
	 * @return {@link List<StandardRegistryEntity>}
	 */
	List<StandardRegistryEntity> getEntitiesInPackage(String packageName);
}

package org.molgenis.model.registry.services;

import org.molgenis.data.meta.model.Package;
import org.molgenis.model.registry.model.ModelRegistryEntity;
import org.molgenis.model.registry.model.ModelRegistrySearch;
import org.molgenis.model.registry.model.ModelRegistryTag;

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
	 * @return {@link ModelRegistrySearch}
	 */
	ModelRegistrySearch search(String query, int offSet, int number);

	/**
	 * <p>Return {@link ModelRegistryTag}-list</p>
	 *
	 * @param pkg {@link Package}
	 * @return {@link List<  ModelRegistryTag  >}
	 */
	List<ModelRegistryTag> getTagsForPackage(Package pkg);

	/**
	 * <p>Return {@link  ModelRegistryEntity}-list</p>
	 *
	 * @param packageName package-name
	 * @return {@link List< ModelRegistryEntity >}
	 */
	List<ModelRegistryEntity> getEntitiesInPackage(String packageName);
}

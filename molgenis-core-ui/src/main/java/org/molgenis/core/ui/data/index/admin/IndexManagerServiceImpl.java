package org.molgenis.core.ui.data.index.admin;

import org.molgenis.data.*;
import org.molgenis.data.index.IndexService;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Elasticsearch index manager service that handles authorization
 */
@Service
public class IndexManagerServiceImpl implements IndexManagerService
{
	private final DataService dataService;
	private final IndexService indexService;

	public IndexManagerServiceImpl(DataService dataService, IndexService indexService)
	{
		this.dataService = requireNonNull(dataService);
		this.indexService = requireNonNull(indexService);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_PLUGIN_READ_indexmanager')")
	public List<EntityType> getIndexedEntities()
	{
		// collect indexed repos
		List<EntityType> indexedEntityTypeList = new ArrayList<>();
		dataService.getEntityTypeIds().forEach(entityTypeId ->
		{
			Repository<Entity> repository = dataService.getRepository(entityTypeId);
			if (repository != null && repository.getCapabilities().contains(RepositoryCapability.INDEXABLE))
			{
				indexedEntityTypeList.add(repository.getEntityType());
			}
		});

		// sort indexed repos by entity label
		indexedEntityTypeList.sort(Comparator.comparing(EntityType::getLabel));
		return indexedEntityTypeList;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public void rebuildIndex(String entityTypeId)
	{
		Repository<Entity> repository = dataService.getRepository(entityTypeId);
		if (!repository.getCapabilities().contains(RepositoryCapability.INDEXABLE))
		{
			throw new MolgenisDataAccessException("Repository [" + entityTypeId + "] is not an indexed repository");
		}
		indexService.rebuildIndex(repository);
	}
}

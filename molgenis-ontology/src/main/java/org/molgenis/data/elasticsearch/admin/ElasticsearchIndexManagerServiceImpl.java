package org.molgenis.data.elasticsearch.admin;

import org.molgenis.data.*;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Elasticsearch index manager service that handles authorization
 */
@Service
public class ElasticsearchIndexManagerServiceImpl implements ElasticsearchIndexManagerService
{
	private final DataService dataService;
	private final ElasticsearchService elasticsearchService;

	@Autowired
	public ElasticsearchIndexManagerServiceImpl(DataService dataService, @SuppressWarnings("SpringJavaAutowiringInspection") ElasticsearchService elasticsearchService)
	{
		this.dataService = requireNonNull(dataService);
		this.elasticsearchService = requireNonNull(elasticsearchService);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_PLUGIN_READ_indexmanager')")
	public List<EntityType> getIndexedEntities()
	{
		// collect indexed repos
		List<EntityType> indexedEntityTypeList = new ArrayList<>();
		dataService.getEntityNames().forEach(entityName ->
		{
			Repository<Entity> repository = dataService.getRepository(entityName);
			if (repository != null && repository.getCapabilities().contains(RepositoryCapability.INDEXABLE))
			{
				indexedEntityTypeList.add(repository.getEntityType());
			}
		});

		// sort indexed repos by entity label
		Collections.sort(indexedEntityTypeList, (e1, e2) -> e1.getLabel().compareTo(e2.getLabel()));
		return indexedEntityTypeList;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public void rebuildIndex(String entityName)
	{
		Repository<Entity> repository = dataService.getRepository(entityName);
		if (!repository.getCapabilities().contains(RepositoryCapability.INDEXABLE))
		{
			throw new MolgenisDataAccessException("Repository [" + entityName + "] is not an indexed repository");
		}
		elasticsearchService.rebuildIndex(repository);
	}
}

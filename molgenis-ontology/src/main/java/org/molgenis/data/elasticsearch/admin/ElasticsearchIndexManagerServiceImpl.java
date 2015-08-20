package org.molgenis.data.elasticsearch.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch index manager service that handles authorization
 */
@Service
public class ElasticsearchIndexManagerServiceImpl implements ElasticsearchIndexManagerService
{
	private final DataService dataService;

	@Autowired
	public ElasticsearchIndexManagerServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_PLUGIN_READ_INDEXMANAGER')")
	public List<EntityMetaData> getIndexedEntities()
	{
		// collect indexed repos
		List<EntityMetaData> indexedEntityMetaDataList = new ArrayList<EntityMetaData>();
		for (String entityName : dataService.getEntityNames())
		{
			Repository repository = dataService.getRepository(entityName);
			if (repository instanceof IndexedRepository)
			{
				indexedEntityMetaDataList.add(repository.getEntityMetaData());
			}
		}

		// sort indexed repos by entity label
		Collections.sort(indexedEntityMetaDataList, new Comparator<EntityMetaData>()
		{
			@Override
			public int compare(EntityMetaData e1, EntityMetaData e2)
			{
				return e1.getLabel().compareTo(e2.getLabel());
			}
		});
		return indexedEntityMetaDataList;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public void rebuildIndex(String entityName)
	{
		Repository repository = dataService.getRepository(entityName);
		if (!(repository instanceof IndexedRepository))
		{
			throw new MolgenisDataAccessException("Repository [" + entityName + "] is not an indexed repository");
		}
		((IndexedRepository) repository).rebuildIndex();
	}
}

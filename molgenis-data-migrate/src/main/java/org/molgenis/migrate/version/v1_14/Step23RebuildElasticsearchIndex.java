package org.molgenis.migrate.version.v1_14;

import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Rebuilds Elasticsearch index due to changes in mapping for integers fields
 */
@Component
public class Step23RebuildElasticsearchIndex extends MolgenisUpgrade implements
		ApplicationListener<ContextRefreshedEvent>
{
	private final DataService dataService;
	private boolean enabled = false;

	@Autowired
	public Step23RebuildElasticsearchIndex(DataService dataService)
	{
		super(22, 23);
		this.dataService = dataService;
	}

	@Override
	public void upgrade()
	{
		this.enabled = true;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (this.enabled)
		{
			RepositoryCollection elasticSearchRepositoryCollection = dataService.getMeta()
					.getBackend(ElasticsearchRepositoryCollection.NAME);
			
			// List of Elasticsearch entities to rebuild index
			List<String> entityNames = Lists.newArrayList(elasticSearchRepositoryCollection.getEntityNames());

			RunAsSystemProxy.runAsSystem(() -> {
				entityNames.spliterator().forEachRemaining(
						e -> elasticSearchRepositoryCollection.getRepository(e).rebuildIndex());
			});
		}

	}
}

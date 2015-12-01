package org.molgenis.migrate.version.v1_14;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.framework.MolgenisUpgrade;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Rebuilds Elasticsearch index due to changes in mapping for integers fields
 */
@Component
public class Step23RebuildElasticsearchIndex extends MolgenisUpgrade implements
		ApplicationListener<ContextRefreshedEvent>
{
	private final MetaDataService metaDataService;
	private boolean enabled = false;

	@Autowired
	public Step23RebuildElasticsearchIndex(MetaDataService metaDataService)
	{
		super(22, 23);
		this.metaDataService = metaDataService;
	}

	@Override
	public void upgrade()
	{
		enabled = true;
		// no operation, index is rebuild after migration steps have been executed
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (enabled)
		{
			RepositoryCollection elasticSearchRepositoryCollection = metaDataService
					.getBackend(ElasticsearchRepositoryCollection.NAME);
			RunAsSystemProxy.runAsSystem(() -> {
				elasticSearchRepositoryCollection.forEach(e -> e.rebuildIndex());
			});
		}

	}
}

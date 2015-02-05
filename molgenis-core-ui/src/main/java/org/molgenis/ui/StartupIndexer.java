package org.molgenis.ui;

import javax.annotation.PostConstruct;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * Reindexes CrudRepositories at startup when index is missing
 */
public abstract class StartupIndexer implements Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(StartupIndexer.class);
	private final SearchService searchService;

	public StartupIndexer(SearchService searchService)
	{
		this.searchService = searchService;
	}

	@PostConstruct
	public void indexReposIfIndexNotExists()
	{
		if (indexExists())
		{
			LOG.info("Index detected, no need to reindex.");
		}
		else
		{
			LOG.info("Missing index, reindexing... ");
			indexRepos();
		}
	}

	private boolean indexExists()
	{
		return searchService.hasMapping(new EntityMetaDataMetaData());
	}

	private void indexRepos()
	{
		getCrudRepositories().forEach(repo -> searchService.rebuildIndex(repo, repo.getEntityMetaData()));
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}

	protected abstract Iterable<CrudRepository> getCrudRepositories();
}

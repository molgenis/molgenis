package org.molgenis.data.index;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class IndexedRepositoryDecoratorFactory
{
	private final SearchService searchService;
	private final IndexJobScheduler indexJobScheduler;

	IndexedRepositoryDecoratorFactory(SearchService searchService, IndexJobScheduler indexJobScheduler)
	{
		this.searchService = requireNonNull(searchService);
		this.indexJobScheduler = requireNonNull(indexJobScheduler);
	}

	public IndexedRepositoryDecorator create(Repository<Entity> delegateRepository)
	{
		return new IndexedRepositoryDecorator(delegateRepository, searchService, indexJobScheduler);
	}
}

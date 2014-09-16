package org.molgenis.data.jpa;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Register the JpaRepositories by the DataService
 */
@Component
public class JpaRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;
	private final RepositoryCollection repositoryCollection;
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;

	@Autowired
	public JpaRepositoryRegistrator(DataService dataService,
			@Qualifier("JpaRepositoryCollection") RepositoryCollection repositoryCollection,
			RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (repositoryCollection == null) throw new IllegalArgumentException("JpaRepositoryCollection is missing");
		this.dataService = dataService;
		this.repositoryCollection = repositoryCollection;
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		for (String name : repositoryCollection.getEntityNames())
		{
			Repository repository = repositoryCollection.getRepositoryByEntityName(name);

			// apply repository decorators (e.g. security, indexing, validation)
			dataService.addRepository(repositoryDecoratorFactory.createDecoratedRepository(repository));
		}
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

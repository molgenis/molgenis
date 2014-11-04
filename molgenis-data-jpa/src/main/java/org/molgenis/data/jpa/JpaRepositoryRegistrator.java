package org.molgenis.data.jpa;

import org.apache.log4j.Logger;
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
	private static final Logger LOG = Logger.getLogger(JpaRepositoryRegistrator.class);

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
		LOG.info("Registering JPA repositories ...");
		for (String name : repositoryCollection.getEntityNames())
		{
			LOG.debug("Registering JPA repository [" + name + "]");
			Repository repository = repositoryCollection.getRepositoryByEntityName(name);

			// apply repository decorators (e.g. security, indexing, validation)
			dataService.addRepository(repositoryDecoratorFactory.createDecoratedRepository(repository));
		}
		LOG.info("Registered JPA repositories");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

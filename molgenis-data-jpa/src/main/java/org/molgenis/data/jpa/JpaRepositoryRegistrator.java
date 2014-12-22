package org.molgenis.data.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jpa.importer.JpaImportService;
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
	private static final Logger LOG = LoggerFactory.getLogger(JpaRepositoryRegistrator.class);

	private final DataService dataService;
	private final RepositoryCollection repositoryCollection;
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;
	private final JpaImportService jpaImportService;
	private final ImportServiceFactory importServiceFactory;

	@Autowired
	public JpaRepositoryRegistrator(DataService dataService,
			@Qualifier("JpaRepositoryCollection") RepositoryCollection repositoryCollection,
			RepositoryDecoratorFactory repositoryDecoratorFactory, JpaImportService jpaImportService,
			ImportServiceFactory importServiceFactory)
	{
		this.dataService = dataService;
		this.repositoryCollection = repositoryCollection;
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
		this.jpaImportService = jpaImportService;
		this.importServiceFactory = importServiceFactory;
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

		importServiceFactory.addImportService(jpaImportService);
		LOG.info("Registered JPA importer");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

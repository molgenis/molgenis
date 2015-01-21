package org.molgenis.data.jpa;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.jpa.importer.JpaImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final JpaImportService jpaImportService;
	private final ImportServiceFactory importServiceFactory;

	@Autowired
	public JpaRepositoryRegistrator(DataService dataService,
			@Qualifier("JpaRepositoryCollection") RepositoryCollection repositoryCollection,
			JpaImportService jpaImportService, ImportServiceFactory importServiceFactory)
	{
		this.dataService = dataService;
		this.repositoryCollection = repositoryCollection;
		this.jpaImportService = jpaImportService;
		this.importServiceFactory = importServiceFactory;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		for (String name : repositoryCollection.getEntityNames())
		{
			Repository repository = repositoryCollection.getRepository(name);
			dataService.getMeta().addEntityMeta(repository.getEntityMetaData());
		}

		importServiceFactory.addImportService(jpaImportService);
		LOG.info("Registered JPA importer");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

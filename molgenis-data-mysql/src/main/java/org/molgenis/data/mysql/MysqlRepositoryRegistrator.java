package org.molgenis.data.mysql;

import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * Register the JpaRepositories by the DataService
 */
public class MysqlRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(MysqlRepositoryRegistrator.class);

	private final MysqlRepositoryCollection repositoryCollection;
	private final ImportServiceFactory importServiceFactory;
	private final ImportService emxImportService;

	public MysqlRepositoryRegistrator(MysqlRepositoryCollection repositoryCollection,
			ImportServiceFactory importServiceFactory, ImportService emxImportService)
	{
		this.repositoryCollection = repositoryCollection;
		this.importServiceFactory = importServiceFactory;
		this.emxImportService = emxImportService;
		LOG.debug("MysqlRepositoryRegistrator: initialized");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		LOG.info("Registering MySQL repositories ...");
		repositoryCollection.registerMysqlRepos();
		importServiceFactory.addImportService(emxImportService);
		LOG.info("Registered MySQL repositories");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

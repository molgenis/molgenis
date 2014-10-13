package org.molgenis.data.mysql;

import org.apache.log4j.Logger;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * Register the JpaRepositories by the DataService
 */
public class MysqlRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger logger = Logger.getLogger(MysqlRepositoryRegistrator.class);
	private final MysqlRepositoryCollection repositoryCollection;
	private final ImportServiceFactory importServiceFactory;
	private final ImportService emxImportService;

	public MysqlRepositoryRegistrator(MysqlRepositoryCollection repositoryCollection,
			ImportServiceFactory importServiceFactory, ImportService emxImportService)
	{
		this.repositoryCollection = repositoryCollection;
		this.importServiceFactory = importServiceFactory;
		this.emxImportService = emxImportService;
		logger.debug("MysqlRepositoryRegistrator: initialized");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		logger.info("Registering MySQL repositories ...");
		repositoryCollection.registerMysqlRepos();
		importServiceFactory.addImportService(emxImportService);
		logger.info("Registered MySQL repositories");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

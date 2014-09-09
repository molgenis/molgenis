package org.molgenis.data.mysql;

import org.apache.log4j.Logger;
import org.molgenis.data.importer.EmxImportServiceImpl;
import org.molgenis.data.importer.ImportServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Register the JpaRepositories by the DataService
 */
@Component
public class MysqlRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger logger = Logger.getLogger(MysqlRepositoryRegistrator.class);
	private final MysqlRepositoryCollection repositoryCollection;
	private final ImportServiceFactory importServiceFactory;
	private final EmxImportServiceImpl emxImportServiceImpl;

	@Autowired
	public MysqlRepositoryRegistrator(MysqlRepositoryCollection repositoryCollection,
			ImportServiceFactory importServiceFactory, EmxImportServiceImpl emxImportServiceImpl)
	{
		this.repositoryCollection = repositoryCollection;
		this.importServiceFactory = importServiceFactory;
		this.emxImportServiceImpl = emxImportServiceImpl;
		logger.debug("MysqlRepositoryRegistrator: initialized");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		repositoryCollection.registerMysqlRepos();
		importServiceFactory.addImportService(emxImportServiceImpl);
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

package org.molgenis.data.mysql;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
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

	@Autowired
	public MysqlRepositoryRegistrator(DataService dataService, MysqlRepositoryCollection repositoryCollection)
	{
		if (repositoryCollection == null) throw new IllegalArgumentException("MysqlRepositoryCollection is missing");
		this.repositoryCollection = repositoryCollection;
		logger.debug("MysqlRepositoryRegistrator: initialized");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		repositoryCollection.registerMysqlRepos();
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

package org.molgenis.data.mysql;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Register the JpaRepositories by the DataService
 */
@Component
public class MysqlRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;
	private final RepositoryCollection repositoryCollection;

	@Autowired
	public MysqlRepositoryRegistrator(DataService dataService,
			MysqlRepositoryCollection repositoryCollection)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (repositoryCollection == null) throw new IllegalArgumentException("MysqlRepositoryCollection is missing");
		this.dataService = dataService;
		this.repositoryCollection = repositoryCollection;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		for (String name : repositoryCollection.getEntityNames())
		{
			dataService.addRepository(repositoryCollection.getRepositoryByEntityName(name));
		}
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

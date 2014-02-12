package org.molgenis.data.jpa;

import org.molgenis.data.DataService;
import org.molgenis.data.RepositorySource;
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
	private final RepositorySource repositorySource;

	@Autowired
	public JpaRepositoryRegistrator(DataService dataService, @Qualifier("JpaRepositorySource")
	RepositorySource repositorySource)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (repositorySource == null) throw new IllegalArgumentException("JpaRepositorySource is missing");
		this.dataService = dataService;
		this.repositorySource = repositorySource;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		dataService.addRepositories(repositorySource);
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}

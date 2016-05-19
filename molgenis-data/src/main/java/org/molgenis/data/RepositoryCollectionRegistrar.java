package org.molgenis.data;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and registers {@link RepositoryCollection} beans with the {@link RepositoryCollectionRegistry}
 */
@Component
public class RepositoryCollectionRegistrar
{
	private final RepositoryCollectionRegistry repositoryCollectionRegistry;

	@Autowired
	public RepositoryCollectionRegistrar(RepositoryCollectionRegistry repositoryCollectionRegistry)
	{
		this.repositoryCollectionRegistry = repositoryCollectionRegistry;
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, RepositoryCollection> repositoryCollectionMap = ctx.getBeansOfType(RepositoryCollection.class);
		repositoryCollectionMap.values().forEach(this::register);
	}

	private void register(RepositoryCollection repositoryCollection)
	{
		repositoryCollectionRegistry.addRepositoryCollection(repositoryCollection);
	}
}

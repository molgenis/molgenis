package org.molgenis.data;

import java.util.Map;

import org.molgenis.data.transaction.UnknownRepositoryCollectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and registers {@link RepositoryCollection} beans with the {@link RepositoryCollectionRegistry}
 */
@Component
public class RepositoryCollectionBootstrapper
{
	private final RepositoryCollectionRegistry repoCollectionRegistry;

	@Autowired
	public RepositoryCollectionBootstrapper(RepositoryCollectionRegistry repoCollectionRegistry)
	{
		this.repoCollectionRegistry = repoCollectionRegistry;
	}

	public void bootstrap(ContextRefreshedEvent event, String defaultRepoCollectionName)
	{
		// register repository collections
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, RepositoryCollection> repoCollectionMap = ctx.getBeansOfType(RepositoryCollection.class);
		repoCollectionMap.values().forEach(this::register);

		// set default repository collection
		RepositoryCollection defaultRepoCollection = repoCollectionMap.values().stream()
				.filter(repoCollection -> repoCollection.getName().equals(defaultRepoCollectionName)).findFirst()
				.orElse(null);
		if (defaultRepoCollection == null)
		{
			throw new UnknownRepositoryCollectionException(defaultRepoCollectionName);
		}
		repoCollectionRegistry.setDefaultRepoCollection(defaultRepoCollection);
	}

	private void register(RepositoryCollection repositoryCollection)
	{
		repoCollectionRegistry.addRepositoryCollection(repositoryCollection);
	}
}

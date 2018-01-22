package org.molgenis.data;

import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;

/**
 * Discovers and registers {@link RepositoryCollection} beans with the {@link RepositoryCollectionRegistry}
 */
@Component
public class RepositoryCollectionBootstrapper
{
	private final RepositoryCollectionRegistry repoCollectionRegistry;
	private EntityTypeMetadata entityTypeMetadata;

	public RepositoryCollectionBootstrapper(RepositoryCollectionRegistry repoCollectionRegistry,
			EntityTypeMetadata entityTypeMetadata)
	{
		this.repoCollectionRegistry = repoCollectionRegistry;
		this.entityTypeMetadata = entityTypeMetadata;
	}

	public void bootstrap(ContextRefreshedEvent event, String defaultRepoCollectionName)
	{
		// register repository collections
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, RepositoryCollection> repoCollectionMap = ctx.getBeansOfType(RepositoryCollection.class);
		repoCollectionMap.values().forEach(this::register);

		// set EntityTypeMetadata backend enum options
		List<String> repoNames = repoCollectionMap.values()
												  .stream()
												  .map(RepositoryCollection::getName)
												  .collect(toList());
		repoNames.sort(naturalOrder());
		entityTypeMetadata.setBackendEnumOptions(repoNames);

		// set default repository collection
		RepositoryCollection defaultRepoCollection = repoCollectionMap.values()
																	  .stream()
																	  .filter(repoCollection -> repoCollection.getName()
																											  .equals(defaultRepoCollectionName))
																	  .findFirst()
																	  .orElse(null);
		if (defaultRepoCollection == null)
		{
			throw new UnknownRepositoryCollectionException(defaultRepoCollectionName);
		}
		repoCollectionRegistry.setDefaultRepoCollection(defaultRepoCollection);
		entityTypeMetadata.setDefaultBackend(defaultRepoCollection.getName());
	}

	private void register(RepositoryCollection repositoryCollection)
	{
		repoCollectionRegistry.addRepositoryCollection(repositoryCollection);
	}
}

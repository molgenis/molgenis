package org.molgenis.bootstrap.populate;

import com.google.common.collect.Multimap;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.molgenis.util.stream.MultimapCollectors.toArrayListMultimap;

/**
 * Discovers {@link SystemEntityRegistry application system entity registries} and populates an empty database with
 * these entities.
 */
@Component
public class SystemEntityPopulator
{
	private final DataService dataService;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;

	public SystemEntityPopulator(DataService dataService, EntityTypeDependencyResolver entityTypeDependencyResolver)
	{
		this.dataService = requireNonNull(dataService);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
	}

	void populate(ContextRefreshedEvent event)
	{
		// discover system entity registries
		ApplicationContext applicationContext = event.getApplicationContext();
		SystemEntityRegistry systemEntityRegistry = applicationContext.getBean(SystemEntityRegistry.class);
		if (systemEntityRegistry != null)
		{
			populate(systemEntityRegistry);
		}
	}

	private void populate(SystemEntityRegistry systemEntityRegistry)
	{
		// sort entities by type
		Multimap<EntityType, Entity> entityByTypeMap = systemEntityRegistry.getEntities()
																		   .stream()
																		   .collect(toArrayListMultimap(
																				   Entity::getEntityType,
																				   Function.identity()));

		// sort entity types
		List<EntityType> sortedEntityTypes = entityTypeDependencyResolver.resolve(entityByTypeMap.keySet());

		// persist entity types
		sortedEntityTypes.forEach(entityType -> persist(entityType, entityByTypeMap.get(entityType)));
	}

	private void persist(EntityType entityType, Collection<Entity> entities)
	{
		dataService.add(entityType.getId(), entities.stream());
	}
}

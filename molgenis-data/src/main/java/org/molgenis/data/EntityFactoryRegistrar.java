package org.molgenis.data;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Discovers and registers entity factories with the entity factory registry.
 *
 * @see EntityFactory
 * @see EntityFactoryRegistry
 */
@Component
public class EntityFactoryRegistrar
{
	private final EntityFactoryRegistry entityFactoryRegistry;

	public EntityFactoryRegistrar(EntityFactoryRegistry entityFactoryRegistry)
	{
		this.entityFactoryRegistry = requireNonNull(entityFactoryRegistry);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, EntityFactory> entityFactoryMap = ctx.getBeansOfType(EntityFactory.class);
		entityFactoryMap.values().forEach(this::registerStaticEntityFactory);
	}

	private void registerStaticEntityFactory(EntityFactory untypedEntityFactory)
	{
		entityFactoryRegistry.registerStaticEntityFactory((EntityFactory<? extends Entity, ?>) untypedEntityFactory);
	}
}

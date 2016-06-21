package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

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

	@Autowired
	public EntityFactoryRegistrar(EntityFactoryRegistry entityFactoryRegistry)
	{
		this.entityFactoryRegistry = requireNonNull(entityFactoryRegistry);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, EntityFactory> entityFactoryMap = ctx.getBeansOfType(EntityFactory.class);
		entityFactoryMap.values().forEach(entityFactoryRegistry::registerStaticEntityFactory);
	}
}

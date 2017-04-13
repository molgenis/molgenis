package org.molgenis.data;

import com.google.common.collect.Maps;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Registry containing all static entity factories.
 *
 * @see EntityFactory
 * @see org.molgenis.data.support.StaticEntity
 */
@Component
public class EntityFactoryRegistry
{
	private final Map<String, EntityFactory<? extends Entity, ?>> staticEntityFactoryMap;

	public EntityFactoryRegistry()
	{
		this.staticEntityFactoryMap = Maps.newHashMap();
	}

	/**
	 * Registers a static entity factory
	 *
	 * @param staticEntityFactory static entity factory
	 * @param <E>                 static entity type (e.g. Tag, Language, Package)
	 */
	<E extends Entity> void registerStaticEntityFactory(EntityFactory<E, ?> staticEntityFactory)
	{
		String entityTypeId = staticEntityFactory.getEntityTypeId();
		staticEntityFactoryMap.put(entityTypeId, staticEntityFactory);
	}

	/**
	 * Returns a entity factory for the given entity meta data.
	 *
	 * @param entityType entity meta data
	 * @return static entity factory or null if no factory exists for the given meta data.
	 */
	EntityFactory<? extends Entity, ?> getEntityFactory(EntityType entityType)
	{
		return staticEntityFactoryMap.get(entityType.getId());
	}
}

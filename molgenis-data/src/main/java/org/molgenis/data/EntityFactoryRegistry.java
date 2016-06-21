package org.molgenis.data;

import java.util.Map;

import org.molgenis.data.meta.model.EntityMetaData;

import com.google.common.collect.Maps;

/**
 * Registry containing all static entity factories.
 *
 * @see EntityFactory
 * @see org.molgenis.data.support.StaticEntity
 */
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
		String entityName = staticEntityFactory.getEntityName();
		staticEntityFactoryMap.put(entityName, staticEntityFactory);
	}

	/**
	 * Returns a entity factory for the given entity meta data.
	 *
	 * @param entityMeta entity meta data
	 * @return static entity factory or null if no factory exists for the given meta data.
	 */
	EntityFactory<? extends Entity, ?> getEntityFactory(EntityMetaData entityMeta)
	{
		return staticEntityFactoryMap.get(entityMeta.getName());
	}
}

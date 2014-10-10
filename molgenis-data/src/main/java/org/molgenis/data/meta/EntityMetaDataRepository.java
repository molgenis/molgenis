package org.molgenis.data.meta;

import org.molgenis.data.EntityMetaData;

public interface EntityMetaDataRepository
{
	/**
	 * Returns an iterable over all entity meta data
	 * 
	 * @return
	 */
	Iterable<EntityMetaData> getEntityMetaDatas();

	/**
	 * Gets the entity meta data for the given entity
	 * 
	 * @param name
	 * @return
	 */
	EntityMetaData getEntityMetaData(String fullyQualifiedName);

	/**
	 * Adds entity meta data for a new entity
	 * 
	 * @param entityMetaData
	 */
	void addEntityMetaData(EntityMetaData entityMetaData);
}
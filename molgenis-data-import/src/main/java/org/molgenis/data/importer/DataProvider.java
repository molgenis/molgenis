package org.molgenis.data.importer;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;

/**
 * Data provider used as source for persisting data.
 *
 * @see DataPersister
 */
public interface DataProvider
{
	/**
	 * Returns all metadata.
	 */
	Stream<EntityType> getEntityTypes();

	/**
	 * Returns whether data is available for the given metadata.
	 */
	boolean hasEntities(EntityType entityType);

	/**
	 * Returns data for the given metadata.
	 */
	Stream<Entity> getEntities(EntityType entityType);
}

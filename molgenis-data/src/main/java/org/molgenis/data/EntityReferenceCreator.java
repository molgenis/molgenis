package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;

public interface EntityReferenceCreator
{
	/**
	 * Get an instance, whose state may be lazily fetched. If the requested instance does not exist in the repository,
	 * an <code>UnknownEntityException</code> is thrown when the instance state is first accessed.
	 *
	 * @param entityType entity meta data
	 * @param id         entity identifier
	 * @return entity
	 */
	Entity getReference(EntityType entityType, Object id);

	/**
	 * Get instances, whose state may be lazily fetched. If a requested instance does not exist in the repository, an
	 * <code>UnknownEntityException</code> is thrown when the instance state is first accessed.
	 *
	 * @param entityType entity meta data
	 * @param ids        entity identifiers
	 * @return entities
	 */
	Iterable<Entity> getReferences(EntityType entityType, Iterable<?> ids);
}

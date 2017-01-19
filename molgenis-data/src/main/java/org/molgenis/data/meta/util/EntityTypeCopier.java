package org.molgenis.data.meta.util;

import org.molgenis.data.meta.model.EntityType;

/**
 * Wrapper for {@link EntityType#newInstance(EntityType)} to improve testability.
 */
public interface EntityTypeCopier
{
	/**
	 * Returns a shallow copy of an entity type
	 *
	 * @param entityType entity type
	 * @return shallow copy of entity type
	 */
	EntityType copy(EntityType entityType);
}

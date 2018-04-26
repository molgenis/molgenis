package org.molgenis.data.listeners;

import org.molgenis.data.Entity;

/**
 * Entity listeners can be added to repositories to listen to changes in entities with a given id.
 */
public interface EntityListener
{
	/**
	 * Returns the entity type id
	 *
	 * @return entity type id
	 */
	Object getEntityId();

	/**
	 * Callback that is fired when the entity with the given id is updated.
	 *
	 * @param entity the updated entity
	 */
	void postUpdate(Entity entity);
}

package org.molgenis.data;

/**
 * Entity listeners can be added to repositories to listen to changes in entities with a given id.
 */
public interface EntityListener
{
	/**
	 * Returns the entity id
	 * 
	 * @return entity entity id
	 */
	public Object getEntityId();

	/**
	 * Callback that is fired when the entity with the given id is updated.
	 * 
	 * @param entity
	 *            the updated entity
	 */
	public void postUpdate(Entity entity);
}

package org.molgenis.data;

import org.molgenis.data.meta.SystemEntity;

/**
 * Entity factory that creates {@link Entity} instances.
 *
 * @param <E> entity type
 * @param <P> entity id type
 */
public interface EntityFactory<E extends SystemEntity, P>
{
	/**
	 * Creates an entity
	 *
	 * @return new entity
	 */
	E create();

	/**
	 * Creates an entity with the given id
	 *
	 * @param entityId entity id
	 * @return new entity with id
	 */
	E create(P entityId);
}

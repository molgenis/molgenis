package org.molgenis.data;

import org.molgenis.data.meta.SystemEntity;

/**
 * Entity factory that creates {@link SystemEntity} instances.
 *
 * @param <E> entity type
 * @param <P> entity id type
 */
public interface SystemEntityFactory<E extends SystemEntity, P>
{
	/**
	 * Returns entity class
	 *
	 * @return entity class
	 */
	Class<E> getEntityClass();

	/**
	 * Creates an entity.
	 *
	 * @return new entity
	 */
	E create();

	/**
	 * Creates an entity with the given id.
	 *
	 * @param entityId entity id
	 * @return new entity with id
	 */
	E create(P entityId);

	/**
	 * Creates an entity based on the given untyped entity.
	 *
	 * @param entity untyped entity
	 * @return typed entity
	 */
	E create(Entity entity);
}

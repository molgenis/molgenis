package org.molgenis.data;

/**
 * Entity factory that creates {@link Entity} instances.
 *
 * @param <E> entity type
 * @param <P> entity id type
 */
public interface EntityFactory<E extends Entity, P>
{
	/**
	 * Returns entity type id
	 *
	 * @return entity type id
	 */
	String getEntityTypeId();

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

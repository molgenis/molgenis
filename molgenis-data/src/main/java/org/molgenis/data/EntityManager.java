package org.molgenis.data;

import java.util.stream.Stream;

import org.molgenis.data.meta.EntityMetaData;

/**
 * Entity manager responsible for creating entity references and resolving references of reference attributes.
 */
public interface EntityManager
{
	/**
	 * Creates an entity.
	 *
	 * @param entityMeta entity meta data
	 * @return new entity
	 */
	Entity create(EntityMetaData entityMeta);

	/**
	 * Creates an entity based on the given fetch.
	 *
	 * @param entityMeta entity meta data
	 * @param fetch      entity data fetch
	 * @return new entity
	 */
	Entity create(EntityMetaData entityMeta, Fetch fetch);

	/**
	 * Get an instance, whose state may be lazily fetched. If the requested instance does not exist in the repository,
	 * an <code>UnknownEntityException</code> is thrown when the instance state is first accessed.
	 *
	 * @param entityMeta entity meta data
	 * @param id         entity identifier
	 * @return entity
	 */
	Entity getReference(EntityMetaData entityMeta, Object id);

	/**
	 * Get instances, whose state may be lazily fetched. If a requested instance does not exist in the repository, an
	 * <code>UnknownEntityException</code> is thrown when the instance state is first accessed.
	 *
	 * @param entityMeta entity meta data
	 * @param ids        entity identifiers
	 * @return entities
	 */
	Iterable<Entity> getReferences(EntityMetaData entityMeta, Iterable<?> ids);

	/**
	 * Resolve entities referenced by a given entity based on provided fetch information. Given entity is modified by
	 * setting references.
	 *
	 * @param entityMeta entity meta data
	 * @param entity     entity
	 * @param fetch      entity data fetch
	 * @return entity with resolved references
	 */
	Entity resolveReferences(EntityMetaData entityMeta, Entity entity, Fetch fetch);

	/**
	 * Resolve entities referenced by a given list of entities based on provided fetch information. Given entities are
	 * modified by setting references.
	 *
	 * @param entityMeta entity meta data
	 * @param entities   entities
	 * @param fetch      entity data fetch
	 * @return entities with resolved references
	 */
	Stream<Entity> resolveReferences(EntityMetaData entityMeta, Stream<Entity> entities, Fetch fetch);
}

package org.molgenis.data;

/**
 * Entity manager responsible for creating entity references and resolving references of reference attributes.
 */
public interface EntityManager
{
	/**
	 * Get an instance, whose state may be lazily fetched. If the requested instance does not exist in the repository,
	 * an <code>UnknownEntityException</code> is thrown when the instance state is first accessed.
	 * 
	 * @param entityMeta
	 * @param id
	 * @return entity
	 */
	Entity getReference(EntityMetaData entityMeta, Object id);

	/**
	 * Get instances, whose state may be lazily fetched. If a requested instance does not exist in the repository, an
	 * <code>UnknownEntityException</code> is thrown when the instance state is first accessed.
	 * 
	 * @param entityMeta
	 * @param ids
	 * @return entities
	 */
	Iterable<Entity> getReferences(EntityMetaData entityMeta, Iterable<?> ids);

	/**
	 * Resolve entities referenced by a given entity based on provided fetch information. Given entity is modified by
	 * setting references.
	 * 
	 * @param entityMeta
	 * @param entity
	 * @param fetch
	 * @return
	 */
	Entity resolveReferences(EntityMetaData entityMeta, Entity entity, Fetch fetch);

	/**
	 * Resolve entities referenced by a given list of entities based on provided fetch information. Given entities are
	 * modified by setting references.
	 * 
	 * @param entityMeta
	 * @param entities
	 * @param fetch
	 * @return
	 */
	Iterable<Entity> resolveReferences(EntityMetaData entityMeta, Iterable<Entity> entities, Fetch fetch);
}

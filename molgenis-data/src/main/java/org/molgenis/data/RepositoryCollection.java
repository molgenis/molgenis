package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Repository collection
 */
public interface RepositoryCollection extends Iterable<Repository<Entity>>
{
	/**
	 * @return the name of this backend
	 */
	String getName();

	/**
	 * Streams the {@link Repository}s
	 */
	default Stream<Repository<Entity>> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Returns the capabilities of this repository collection
	 *
	 * @return repository collection capabilities
	 */
	Set<RepositoryCollectionCapability> getCapabilities();

	/**
	 * Creates a new {@link Repository} within this repository collection for the given entity meta data.
	 *
	 * @param entityType entity meta data
	 * @return the created repository
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#WRITABLE}
	 */
	Repository<Entity> createRepository(EntityType entityType);

	/**
	 * Get ids of all the entity types in this source
	 */
	Iterable<String> getEntityTypeIds();

	/**
	 * Get a repository by entity type id
	 *
	 * @throws UnknownEntityTypeException if no repository exists for the given entity name
	 */
	Repository<Entity> getRepository(String id);

	/**
	 * Get a repository for the given entity meta data
	 *
	 * @param entityType entity meta data
	 * @return repository for the given entity meta data
	 */
	Repository<Entity> getRepository(EntityType entityType);

	/**
	 * Check if a repository exists by entity id
	 */
	boolean hasRepository(String id);

	boolean hasRepository(EntityType entityType);

	/**
	 * Deletes the {@link Repository} with the given entity name from this repository collection.
	 *
	 * @param entityType@throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#WRITABLE}
	 */
	void deleteRepository(EntityType entityType);

	/**
	 * Updates a repository. Handles EntityType changes other than the addition/alteration/removal of attributes.
	 *
	 * @param entityType        the existing EntityType
	 * @param updatedEntityType the updated EntityType
	 */
	void updateRepository(EntityType entityType, EntityType updatedEntityType);

	/**
	 * Adds an Attribute to an entityType
	 *
	 * @param entityType entity meta data
	 * @param attribute  attribute to add
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void addAttribute(EntityType entityType, Attribute attribute);

	/**
	 * Updates {@link Repository repositories} for the given updated attribute.
	 *
	 * @param entityType  entity meta data
	 * @param attr        attribute
	 * @param updatedAttr updated attribute
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr);

	/**
	 * Removes an attribute from an entity
	 *
	 * @param entityType entity meta data
	 * @param attr       attribute to remove
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void deleteAttribute(EntityType entityType, Attribute attr);
}

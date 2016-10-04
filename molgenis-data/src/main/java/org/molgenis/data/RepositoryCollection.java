package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;

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
	 * @param entityMeta entity meta data
	 * @return the created repository
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#WRITABLE}
	 */
	Repository<Entity> createRepository(EntityMetaData entityMeta);

	/**
	 * Get names of all the entities in this source
	 */
	Iterable<String> getEntityNames();

	/**
	 * Get a repository by entity name
	 *
	 * @throws UnknownEntityException if no repository exists for the given entity name
	 */
	Repository<Entity> getRepository(String name);

	/**
	 * Get a repository for the given entity meta data
	 *
	 * @param entityMeta entity meta data
	 * @return repository for the given entity meta data
	 */
	Repository<Entity> getRepository(EntityMetaData entityMeta);

	/**
	 * Check if a repository exists by entity name
	 */
	boolean hasRepository(String name);

	boolean hasRepository(EntityMetaData entityMeta);

	/**
	 * Deletes the {@link Repository} with the given entity name from this repository collection.
	 *
	 * @param entityMeta@throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#WRITABLE}
	 */
	void deleteRepository(EntityMetaData entityMeta);

	/**
	 * Adds an Attribute to an EntityMeta
	 *
	 * @param entityMeta entity meta data
	 * @param attribute  attribute to add
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void addAttribute(EntityMetaData entityMeta, Attribute attribute);

	/**
	 * Updates {@link Repository repositories} for the given updated attribute.
	 *
	 * @param entityMetaData entity meta data
	 * @param attr           attribute
	 * @param updatedAttr    updated attribute
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void updateAttribute(EntityMetaData entityMetaData, Attribute attr, Attribute updatedAttr);

	/**
	 * Removes an attribute from an entity
	 *
	 * @param entityMeta entity meta data
	 * @param attr       attribute to remove
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void deleteAttribute(EntityMetaData entityMeta, Attribute attr);

	/**
	 * Returns the language codes defined in the meta data stored in this repository collection.
	 *
	 * @return stream of language codes
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#META_DATA_PERSISTABLE}
	 */
	Stream<String> getLanguageCodes();
}

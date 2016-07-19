package org.molgenis.data;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

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
	 * @throws UnknownEntityException
	 */
	Repository<Entity> getRepository(String name);

	/**
	 * Get a repository for the given entity meta data
	 *
	 * @param entityMeta
	 * @return
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
	 * @param entityName
	 * @param attribute
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void addAttribute(String entityName, AttributeMetaData attribute);

	/**
	 * Updates {@link Repository repositories} for the given updated attribute.
	 *
	 * @param entityMetaData entity meta data
	 * @param attr           attribute
	 * @param updatedAttr
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void updateAttribute(EntityMetaData entityMetaData, AttributeMetaData attr, AttributeMetaData updatedAttr);

	/**
	 * Removes an attribute from an entity
	 *
	 * @param entityName
	 * @param attributeName
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#UPDATABLE}
	 */
	void deleteAttribute(String entityName, String attributeName);

	/**
	 * Returns the language codes defined in the meta data stored in this repository collection.
	 *
	 * @return
	 * @throws UnsupportedOperationException if this repository collection is not {@link RepositoryCollectionCapability#META_DATA_PERSISTABLE}
	 */
	Stream<String> getLanguageCodes();
}

package org.molgenis.data.meta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;

import com.google.common.collect.ImmutableMap;

public interface MetaDataService extends Iterable<RepositoryCollection>
{
	Repository getRepository(String entityName);

	Repository getRepository(EntityMetaData entityMeta);

	/**
	 * Returns whether a {@link Repository} exists for the given entity name. Always returns false for abstract entities.
	 *
	 * @param entityName entity name
	 * @return true if non-abstract entity meta data exists for the given entity name
	 */
	boolean hasRepository(String entityName);

	/**
	 * Sets the backend, in wich the meta data and the user data is saved
	 *
	 * @param backend
	 */
	MetaDataService setDefaultBackend(RepositoryCollection backend);

	/**
	 * Get a backend by name or null if it does not exists
	 *
	 * @param name
	 * @return
	 */
	RepositoryCollection getBackend(String name);

	/**
	 * Get the backend the EntityMetaData belongs to
	 *
	 * @param emd
	 * @return
	 */
	RepositoryCollection getBackend(EntityMetaData emd);

	/**
	 * Get the default backend
	 *
	 * @return
	 */
	RepositoryCollection getDefaultBackend();

	/**
	 * Get all packages
	 *
	 * @return List of Package
	 */
	public List<Package> getPackages();

	/**
	 * Lists all root packages.
	 *
	 * @return Iterable of all root Packages
	 */
	Iterable<Package> getRootPackages();

	/**
	 * Retrieves a package with a given name.
	 *
	 * @param name the name of the Package to retrieve
	 * @return the Package, or null if the package does not exist.
	 */
	Package getPackage(String name);

	/**
	 * Adds a new Package
	 *
	 * @param pack
	 */
	void addPackage(Package pack);

	/**
	 * Gets the entity meta data for a given entity.
	 *
	 * @param name the fullyQualifiedName of the entity
	 * @return EntityMetaData of the entity, or null if the entity does not exist
	 */
	EntityMetaData getEntityMetaData(String name);

	/**
	 * Returns whether {@link EntityMetaData entity meta data} exists for the given entity name.
	 *
	 * @param entityName entity name
	 * @return true if entity meta data exists for the given entity name
	 */
	boolean hasEntityMetaData(String entityName);

	/**
	 * Returns a stream of all {@link EntityMetaData entity meta data}.
	 *
	 * @return all entity meta data
	 */
	Stream<EntityMetaData> getEntityMetaDatas();

	/**
	 * Adds new EntityMeta and creates a new Repository
	 *
	 * @param entityMeta
	 * @return
	 */
	Repository<Entity> addEntityMeta(EntityMetaData entityMeta);

	/**
	 * Deletes an EntityMeta
	 */
	void deleteEntityMeta(String entityName);

	/**
	 * Deletes a list of EntityMetaData
	 *
	 * @param entities
	 */
	void delete(List<EntityMetaData> entities);

	/**
	 * Updates EntityMeta
	 *
	 * @param entityMeta
	 * @return added attributes
	 * <p>
	 * FIXME remove return value or change it to ChangeSet with all changes
	 */
	List<AttributeMetaData> updateEntityMeta(EntityMetaData entityMeta);

	/**
	 * Adds an Attribute to an EntityMeta
	 *
	 * @param entityName
	 * @param attribute
	 */
	void addAttribute(String entityName, AttributeMetaData attribute);

	/**
	 * Deletes an Attribute
	 *
	 * @param entityName
	 * @param attributeName
	 */
	void deleteAttribute(String entityName, String attributeName);

	/**
	 * Check the integration of an entity meta data with existing entities Check only if the existing attributes are the
	 * same as the new attributes
	 *
	 * @param repositoryCollection the new entities
	 * @return
	 */
	LinkedHashMap<String, Boolean> integrationTestMetaData(RepositoryCollection repositoryCollection);

	/**
	 * Check the integration of an entity meta data with existing entities Check only if the existing attributes are the
	 * same as the new attributes
	 *
	 * @param newEntitiesMetaDataMap the new entities in a map where the keys are the names
	 * @param skipEntities           do not check the entities, returns true.
	 * @param defaultPackage         the default package for the entities that does not have a package
	 * @return
	 */
	LinkedHashMap<String, Boolean> integrationTestMetaData(ImmutableMap<String, EntityMetaData> newEntitiesMetaDataMap,
			List<String> skipEntities, String defaultPackage);

	/**
	 * Has backend will check if the requested backend already exists and is registered.
	 *
	 * @param backendName
	 * @return
	 */
	boolean hasBackend(String backendName);

	/**
	 * Returns whether the given {@link EntityMetaData} defines a meta entity such as {@link EntityMetaDataMetaData} or
	 * {@link AttributeMetaData}.
	 *
	 * @param entityMetaData
	 * @return
	 */
	boolean isMetaEntityMetaData(EntityMetaData entityMetaData);
}

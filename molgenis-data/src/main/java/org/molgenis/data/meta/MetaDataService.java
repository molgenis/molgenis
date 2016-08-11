package org.molgenis.data.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.meta.model.Package;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

public interface MetaDataService extends Iterable<RepositoryCollection>
{
	/**
	 * Returns the application language codes
	 *
	 * @return the application language codes
	 */
	Stream<String> getLanguageCodes();

	/**
	 * Returns the repository for the given entity name.
	 *
	 * @param entityName entity name
	 * @return entity repository or null if no repository exists for the entity (e.g. the entity is abstract)
	 * @throws UnknownEntityException if no entity with the given name exists
	 */
	Repository<Entity> getRepository(String entityName);

	/**
	 * Returns the typed repository for the given entity name.
	 *
	 * @param entityName  entity name
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed entity repository or null if no repository exists for the entity (e.g. the entity is abstract)
	 * @throws UnknownEntityException if no entity with the given name exists
	 */
	<E extends Entity> Repository<E> getRepository(String entityName, Class<E> entityClass);

	/**
	 * Returns the repository for the given entity meta data
	 *
	 * @param entityMeta entity meta data
	 * @return entity repository or null if no repository exists for the entity (e.g. the entity is abstract)
	 */
	Repository<Entity> getRepository(EntityMetaData entityMeta);

	/**
	 * Returns the typed repository for the given entity meta data
	 *
	 * @param entityMeta  entity meta data
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed entity repository or null if no repository exists for the entity (e.g. the entity is abstract).
	 */
	<E extends Entity> Repository<E> getRepository(EntityMetaData entityMeta, Class<E> entityClass);

	/**
	 * Returns whether a {@link Repository} exists for the given entity name. Always returns false for abstract entities.
	 *
	 * @param entityName entity name
	 * @return true if non-abstract entity meta data exists for the given entity name
	 */
	boolean hasRepository(String entityName);

	/**
	 * Create a repository for the given entity meta data.
	 *
	 * @param entityMeta entity meta data
	 * @return repository
	 * @throws org.molgenis.data.MolgenisDataException if entity meta data is abstract
	 */
	Repository<Entity> createRepository(EntityMetaData entityMeta);

	/**
	 * Create a typed repository for the given entity meta data.
	 *
	 * @param entityMeta  entity meta data
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed repository
	 * @throws org.molgenis.data.MolgenisDataException if entity meta data is abstract
	 */
	<E extends Entity> Repository<E> createRepository(EntityMetaData entityMeta, Class<E> entityClass);

	/**
	 * Get a backend by name or null if it does not exists
	 *
	 * @param backendName repository collection name
	 * @return repository collection, null if entity meta data is abstract
	 */
	RepositoryCollection getBackend(String backendName);

	/**
	 * Get the backend the EntityMetaData belongs to
	 *
	 * @param entityMeta entity meta data
	 * @return repository collection, null if entity meta data is abstract
	 */
	RepositoryCollection getBackend(EntityMetaData entityMeta);

	/**
	 * Has backend will check if the requested backend already exists and is registered.
	 *
	 * @param backendName backend name
	 * @return true if a repository collection with the given name exists
	 */
	boolean hasBackend(String backendName);

	/**
	 * Get the default backend
	 *
	 * @return the default repository collection
	 */
	RepositoryCollection getDefaultBackend();

	/**
	 * Get all packages
	 *
	 * @return List of Package
	 */
	List<Package> getPackages();

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
	 * @param pack package
	 */
	void addPackage(Package pack);

	/**
	 * Add or update packages
	 *
	 * @param packages packages
	 */
	void upsertPackages(Stream<Package> packages);

	/**
	 * Gets the entity meta data for a given entity.
	 *
	 * @param name the fullyQualifiedName of the entity
	 * @return EntityMetaData of the entity, or null if the entity does not exist
	 */
	EntityMetaData getEntityMetaData(String name);

	/**
	 * Returns a stream of all {@link EntityMetaData entity meta data}.
	 *
	 * @return all entity meta data
	 */
	Stream<EntityMetaData> getEntityMetaDatas();

	/**
	 * Returns a stream of all {@link Repository repositories}.
	 *
	 * @return all repositories
	 */
	Stream<Repository<Entity>> getRepositories();

	/**
	 * Add entity meta data and entity meta data attributes.
	 *
	 * @param entityMeta entity meta data
	 */
	void addEntityMeta(EntityMetaData entityMeta);

	/**
	 * Deletes an EntityMeta
	 *
	 * @param entityName entity name
	 */
	void deleteEntityMeta(String entityName);

	/**
	 * Deletes a list of EntityMetaData
	 *
	 * @param entities
	 */
	void delete(List<EntityMetaData> entities);

	/**
	 * Updates entity meta data and entity meta data attributes.
	 *
	 * @param entityMeta entity meta data
	 * @throws UnknownEntityException if entity meta data does not exist
	 */
	void updateEntityMeta(EntityMetaData entityMeta);

	/**
	 * Adds an Attribute to an EntityMeta
	 *
	 * @param attribute
	 */
	void addAttribute(AttributeMetaData attribute);

	/**
	 * Deletes an Attribute from an Entity
	 *
	 * @param id
	 */
	void deleteAttributeById(Object id);

	/**
	 * Check the integration of an entity meta data with existing entities Check only if the existing attributes are the
	 * same as the new attributes
	 *
	 * @param repositoryCollection the new entities
	 * @return
	 */
	LinkedHashMap<String, Boolean> determineImportableEntities(RepositoryCollection repositoryCollection);

	/**
	 * Returns whether the given {@link EntityMetaData} defines a meta entity such as {@link EntityMetaDataMetaData} or
	 * {@link AttributeMetaData}.
	 *
	 * @param entityMetaData
	 * @return
	 */
	boolean isMetaEntityMetaData(EntityMetaData entityMetaData);

	/**
	 * Returns whether the given {@link EntityMetaData} attributes are compatible with
	 * the attributes of an existing repository with the same name
	 *
	 * @param entityMetaData
	 * @return
	 */
	boolean isEntityMetaDataCompatible(EntityMetaData entityMetaData);
}

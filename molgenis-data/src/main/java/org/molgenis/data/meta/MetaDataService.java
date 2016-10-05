package org.molgenis.data.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
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
	 * @param entityType entity meta data
	 * @return entity repository or null if no repository exists for the entity (e.g. the entity is abstract)
	 */
	Repository<Entity> getRepository(EntityType entityType);

	/**
	 * Returns the typed repository for the given entity meta data
	 *
	 * @param entityType  entity meta data
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed entity repository or null if no repository exists for the entity (e.g. the entity is abstract).
	 */
	<E extends Entity> Repository<E> getRepository(EntityType entityType, Class<E> entityClass);

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
	 * @param entityType entity meta data
	 * @return repository
	 * @throws org.molgenis.data.MolgenisDataException if entity meta data is abstract
	 */
	Repository<Entity> createRepository(EntityType entityType);

	/**
	 * Create a typed repository for the given entity meta data.
	 *
	 * @param entityType  entity meta data
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed repository
	 * @throws org.molgenis.data.MolgenisDataException if entity meta data is abstract
	 */
	<E extends Entity> Repository<E> createRepository(EntityType entityType, Class<E> entityClass);

	/**
	 * Get a backend by name or null if it does not exists
	 *
	 * @param backendName repository collection name
	 * @return repository collection, null if entity meta data is abstract
	 */
	RepositoryCollection getBackend(String backendName);

	/**
	 * Get the backend the EntityType belongs to
	 *
	 * @param entityType entity meta data
	 * @return repository collection, null if entity meta data is abstract
	 */
	RepositoryCollection getBackend(EntityType entityType);

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
	 * @return EntityType of the entity, or null if the entity does not exist
	 */
	EntityType getEntityType(String name);

	/**
	 * Returns a stream of all {@link EntityType entity meta data}.
	 *
	 * @return all entity meta data
	 */
	Stream<EntityType> getEntityTypes();

	/**
	 * Returns a stream of all {@link Repository repositories}.
	 *
	 * @return all repositories
	 */
	Stream<Repository<Entity>> getRepositories();

	/**
	 * Add entity meta data and entity meta data attributes.
	 *
	 * @param entityType entity meta data
	 */
	void addEntityType(EntityType entityType);

	/**
	 * Deletes an entityType
	 *
	 * @param entityName entity name
	 */
	void deleteEntityType(String entityName);

	/**
	 * Deletes a list of EntityType
	 *
	 * @param entities
	 */
	void delete(List<EntityType> entities);

	/**
	 * Updates entity meta data and entity meta data attributes.
	 *
	 * @param entityType entity meta data
	 * @throws UnknownEntityException if entity meta data does not exist
	 */
	void updateEntityType(EntityType entityType);

	/**
	 * Adds an Attribute to an entityType
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
	 * Returns whether the given {@link EntityType} defines a meta entity such as {@link EntityTypeMetadata} or
	 * {@link AttributeMetaData}.
	 *
	 * @param entityType
	 * @return
	 */
	boolean isMetaEntityType(EntityType entityType);

	/**
	 * Returns whether the given {@link EntityType} attributes are compatible with
	 * the attributes of an existing repository with the same name
	 *
	 * @param entityType
	 * @return
	 */
	boolean isEntityTypeCompatible(EntityType entityType);
}

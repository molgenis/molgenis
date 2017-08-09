package org.molgenis.data.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;

public interface MetaDataService extends Iterable<RepositoryCollection>
{
	/**
	 * Returns the repository for the given entity name.
	 *
	 * @return entity repository or null if no repository exists for the entity (e.g. the entity is abstract)
	 * @throws UnknownEntityException if no entity with the given name exists
	 */
	Repository<Entity> getRepository(String entityTypeId);

	/**
	 * Returns the typed repository for the given entity name.
	 *
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed entity repository or null if no repository exists for the entity (e.g. the entity is abstract)
	 * @throws UnknownEntityException if no entity with the given name exists
	 */
	<E extends Entity> Repository<E> getRepository(String entityTypeId, Class<E> entityClass);

	/**
	 * Returns the repository for the given entity type
	 *
	 * @param entityType entity type
	 * @return entity repository or null if no repository exists for the entity (e.g. the entity is abstract)
	 */
	Repository<Entity> getRepository(EntityType entityType);

	/**
	 * Returns the typed repository for the given entity type
	 *
	 * @param entityType  entity type
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed entity repository or null if no repository exists for the entity (e.g. the entity is abstract).
	 */
	<E extends Entity> Repository<E> getRepository(EntityType entityType, Class<E> entityClass);

	/**
	 * Returns whether a {@link Repository} exists for the given entity name. Always returns false for abstract entities.
	 *
	 * @return true if non-abstract entity type exists for the given entity name
	 */
	boolean hasRepository(String entityTypeId); // FIXME use entity type ids instead of entity type fqns

	/**
	 * Create a repository for the given entity type.
	 *
	 * @param entityType entity type
	 * @return repository
	 * @throws org.molgenis.data.MolgenisDataException if entity type is abstract
	 */
	Repository<Entity> createRepository(EntityType entityType);

	/**
	 * Create a typed repository for the given entity type.
	 *
	 * @param entityType  entity type
	 * @param entityClass entity class
	 * @param <E>         entity type
	 * @return typed repository
	 * @throws org.molgenis.data.MolgenisDataException if entity type is abstract
	 */
	<E extends Entity> Repository<E> createRepository(EntityType entityType, Class<E> entityClass);

	/**
	 * Get a backend by name or null if it does not exists
	 *
	 * @param backendName repository collection name
	 * @return repository collection, null if entity type is abstract
	 */
	RepositoryCollection getBackend(String backendName);

	/**
	 * Get the backend the EntityType belongs to
	 *
	 * @param entityType entity type
	 * @return repository collection, null if entity type is abstract
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
	Package getPackage(String name); // FIXME use entity type ids instead of entity type fqns

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
	 * Add or update tags
	 *
	 * @param tags tags
	 */
	void upsertTags(Collection<Tag> tags);

	/**
	 * Gets the entity type for a given entity.
	 *
	 * @param name the fullyQualifiedName of the entity
	 * @return EntityType of the entity, or null if the entity does not exist
	 */
	EntityType getEntityType(String name); // FIXME use entity type ids instead of entity type fqns

	/**
	 * Gets the entity type for a given entity.
	 *
	 * @param entityTypeId the id of the entity
	 * @return EntityType of the entity, or null if the entity does not exist
	 */
	EntityType getEntityTypeById(String entityTypeId); // FIXME remove

	/**
	 * Returns a stream of all {@link EntityType entity type}.
	 *
	 * @return all entity type
	 */
	Stream<EntityType> getEntityTypes();

	/**
	 * Returns a stream of all {@link Repository repositories}.
	 *
	 * @return all repositories
	 */
	Stream<Repository<Entity>> getRepositories();

	/**
	 * Add entity type and entity type attributes.
	 *
	 * @param entityType entity type
	 */
	void addEntityType(EntityType entityType);

	/**
	 * Updates a single existing entity type and entity type attributes.
	 *
	 * @param entityType entity type
	 * @throws UnknownEntityException if entity type does not exist
	 */
	void updateEntityType(EntityType entityType);

	/**
	 * Add or update a collection of entity type and entity type attributes.
	 * Resolves the dependencies between them so that the entities and their metadata get added in proper order.
	 * <p>
	 * Adds ONE_TO_MANY attributes in a two-pass algorithm.
	 * <ol>
	 * <li>Add the Author {@link EntityType} without books attribute and the Book {@link EntityType} with its author
	 * attribute.</li>
	 * <li>Update the Author EntityType adding the books attribute</li>
	 * </ol>
	 *
	 * @param entityTypes {@link EntityType}s to add
	 */
	void upsertEntityTypes(Collection<EntityType> entityTypes);

	/**
	 * Deletes an EntityType
	 */
	void deleteEntityType(String entityTypeId); // FIXME use entity type ids instead of entity type fqns

	/**
	 * Deletes a collection of entity type.
	 *
	 * @param entityTypes entity type collection
	 */
	void deleteEntityType(Collection<EntityType> entityTypes);

	/**
	 * Adds an Attribute to an EntityType
	 *
	 * @param attribute
	 */
	void addAttribute(Attribute attribute);

	/**
	 * Adds attributes to an EntityType
	 *
	 * @param attrs Stream <Attribute>
	 */
	void addAttributes(String entityTypeId, Stream<Attribute> attrs);

	/**
	 * Deletes an Attribute from an Entity
	 *
	 * @param id
	 */
	void deleteAttributeById(Object id);

	/**
	 * Check the integration of an entity type with existing entities Check only if the existing attributes are the
	 * same as the new attributes
	 *
	 * @param repositoryCollection the new entities
	 * @return
	 */
	LinkedHashMap<String, Boolean> determineImportableEntities(RepositoryCollection repositoryCollection);

	/**
	 * Returns whether the given {@link EntityType} defines a meta entity such as {@link EntityTypeMetadata} or
	 * {@link Attribute}.
	 *
	 * @param entityType the EntityType that is checked
	 */
	static boolean isMetaEntityType(EntityType entityType)
	{
		switch (entityType.getId())
		{
			case ENTITY_TYPE_META_DATA:
			case ATTRIBUTE_META_DATA:
			case TAG:
			case PACKAGE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns whether the given {@link EntityType} attributes are compatible with
	 * the attributes of an existing repository with the same name
	 *
	 * @param entityTypeData
	 * @return
	 */
	boolean isEntityTypeCompatible(EntityType entityTypeData);

	/**
	 * Returns all concrete {@link EntityType}s that directly or indirectly extend a given {@link EntityType}.
	 * If the {@link EntityType} is concrete, will return a Stream containing only the given {@link EntityType}.
	 *
	 * @param entityType the {@link EntityType} whose concrete child entity types will be returned
	 * @return Stream containing all concrete children
	 */
	Stream<EntityType> getConcreteChildren(EntityType entityType);
}

package org.molgenis.data.meta;

import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;

public interface MetaDataService extends Iterable<RepositoryCollection> {
  /**
   * Returns the repository for the given entity type identifier.
   *
   * @return optional entity repository
   * @throws UnknownEntityTypeException if no entity type with the given identifier exists
   */
  Optional<Repository<Entity>> getRepository(String entityTypeId);

  /**
   * Returns the typed repository for the given entity type identifier.
   *
   * @param entityClass entity class
   * @param <E> entity type
   * @return optional typed entity repository
   * @throws UnknownEntityTypeException if no entity with the given name exists
   */
  <E extends Entity> Optional<Repository<E>> getRepository(
      String entityTypeId, Class<E> entityClass);

  /**
   * Returns the repository for the given entity type
   *
   * @param entityType entity type
   * @return optional entity repository
   */
  Optional<Repository<Entity>> getRepository(EntityType entityType);

  /**
   * Returns the typed repository for the given entity type
   *
   * @param entityType entity type
   * @param entityClass entity class
   * @param <E> entity type
   * @return optional typed entity repository
   */
  <E extends Entity> Optional<Repository<E>> getRepository(
      EntityType entityType, Class<E> entityClass);

  /**
   * Returns whether a {@link Repository} exists for the given entity name. Always returns false for
   * abstract entities.
   *
   * @return true if non-abstract entity type exists for the given entity name
   */
  boolean hasRepository(String entityTypeId);

  /**
   * Create a repository for the given entity type.
   *
   * @param entityType entity type
   * @return repository
   * @throws RepositoryCreationException if entity type is abstract
   */
  Repository<Entity> createRepository(EntityType entityType);

  /**
   * Create a typed repository for the given entity type.
   *
   * @param entityType entity type
   * @param entityClass entity class
   * @param <E> entity type
   * @return typed repository
   * @throws RepositoryCreationException if entity type is abstract
   */
  <E extends Entity> Repository<E> createRepository(EntityType entityType, Class<E> entityClass);

  /**
   * Get a backend by name
   *
   * @param backendName repository collection name
   * @return optional repository collection
   * @throws UnknownRepositoryCollectionException if no unknown repository collection exists for the
   *     given name
   */
  RepositoryCollection getBackend(String backendName);

  /**
   * Get the backend the EntityType belongs to
   *
   * @param entityType entity type
   * @return repository collection, null if entity type is abstract
   * @throws UnknownRepositoryCollectionException if no unknown repository collection exists for the
   *     given entity type
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
   * Gets the package for a given package identifier.
   *
   * @param packageId package identifier
   * @return the Package, or <tt>null</tt> if the package does not exist.
   */
  Package getPackage(String packageId);

  /**
   * Adds a new Package
   *
   * @param aPackage package
   */
  void addPackage(Package aPackage);

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
   * Returns whether an entity type with the given id exists.
   *
   * @return <tt>true</tt> if entity type exists for the given entity id
   */
  boolean hasEntityType(String entityTypeId);

  /**
   * Gets the entity type for a given entity type identifier.
   *
   * @param entityTypeId the identifier of the entity
   * @return EntityType of the entity, or null if the entity does not exist
   */
  EntityType getEntityType(String entityTypeId);

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
   * @throws UnknownEntityTypeException if entity type does not exist
   */
  void updateEntityType(EntityType entityType);

  /**
   * Add or update a collection of entity type and entity type attributes. Resolves the dependencies
   * between them so that the entities and their metadata get added in proper order.
   *
   * <p>Adds ONE_TO_MANY attributes in a two-pass algorithm.
   *
   * <ol>
   *   <li>Add the Author {@link EntityType} without books attribute and the Book {@link EntityType}
   *       with its author attribute.
   *   <li>Update the Author EntityType adding the books attribute
   * </ol>
   *
   * @param entityTypes {@link EntityType}s to add
   */
  void upsertEntityTypes(Collection<EntityType> entityTypes);

  /** Deletes an EntityType */
  void deleteEntityType(String entityTypeId);

  /**
   * Deletes a collection of entity type.
   *
   * @param entityTypes entity type collection
   */
  void deleteEntityType(Collection<EntityType> entityTypes);

  /** Adds an Attribute to an EntityType */
  void addAttribute(Attribute attribute);

  /** Deletes an Attribute from an Entity */
  void deleteAttributeById(Object id);

  /**
   * Check the integration of an entity type with existing entities Check only if the existing
   * attributes are the same as the new attributes
   *
   * @param repositoryCollection the new entities
   */
  Map<String, Boolean> determineImportableEntities(RepositoryCollection repositoryCollection);

  /**
   * Returns whether the given {@link EntityType} defines a meta entity such as {@link
   * EntityTypeMetadata} or {@link Attribute}.
   *
   * @param entityType the EntityType that is checked
   */
  static boolean isMetaEntityType(EntityType entityType) {
    switch (entityType.getId()) {
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
   * Returns whether the given {@link EntityType} attributes are compatible with the attributes of
   * an existing repository with the same name
   */
  boolean isEntityTypeCompatible(EntityType entityTypeData);

  /**
   * Returns all concrete {@link EntityType}s that directly or indirectly extend a given {@link
   * EntityType}. If the {@link EntityType} is concrete, will return a Stream containing only the
   * given {@link EntityType}.
   *
   * @param entityType the {@link EntityType} whose concrete child entity types will be returned
   * @return Stream containing all concrete children
   */
  Stream<EntityType> getConcreteChildren(EntityType entityType);

  /**
   * Returns all {@link Attribute} that refer to a given {@link EntityType} as a refEntity
   *
   * @param entityTypeId ID of the {@link EntityType} that the attribute refers to
   * @return Stream of referring {@link Attribute}s
   */
  Stream<Attribute> getReferringAttributes(String entityTypeId);
}

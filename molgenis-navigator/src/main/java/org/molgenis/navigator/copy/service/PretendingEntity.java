package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;

import java.util.Map;
import org.molgenis.data.AbstractEntityDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaMetadata;
import org.molgenis.data.meta.model.EntityType;

/**
 * When copying rows from one repository to another, the metadata of these entities will not fit the
 * copied repository and its references because the metadatas will have different IDs. The
 * PretendingEntity acts like it's the newly copied entity by returning the metadata of the copied
 * repository instead of the original.
 */
class PretendingEntity extends AbstractEntityDecorator {

  private final Map<String, EntityType> copiedEntityTypes;

  PretendingEntity(Entity entity, Map<String, EntityType> copiedEntityTypes) {
    super(entity);
    this.copiedEntityTypes = copiedEntityTypes;
  }

  @Override
  public EntityType getEntityType() {
    String id = delegate().getEntityType().getId();
    if (copiedEntityTypes.containsKey(id)) {
      return copiedEntityTypes.get(id);
    } else {
      return delegate().getEntityType();
    }
  }

  @Override
  public Entity getEntity(String attributeName) {
    Entity entity = delegate().getEntity(attributeName);
    return entity != null ? newPretendingEntity(entity) : null;
  }

  /**
   * Because the File datatype has a reference to {@link FileMetaMetadata} it can happen that a
   * typed FileMeta Entity is requested.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <E extends Entity> E getEntity(String attributeName, Class<E> clazz) {
    Entity entity = delegate().getEntity(attributeName, clazz);
    if (clazz.equals(FileMeta.class)) {
      return entity != null ? (E) new FileMeta(newPretendingEntity(entity)) : null;
    } else {
      throw new UnsupportedOperationException("Can't return typed pretending entities");
    }
  }

  @Override
  public Iterable<Entity> getEntities(String attributeName) {
    return stream(delegate().getEntities(attributeName))
        .map(this::newPretendingEntity)
        .collect(toList());
  }

  /**
   * Because the File datatype has a reference to {@link FileMetaMetadata} it can happen that a
   * typed FileMeta Entity is requested.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz) {
    Iterable<E> entities = delegate().getEntities(attributeName, clazz);
    if (clazz.equals(FileMeta.class)) {
      return stream(entities)
          .map(this::newPretendingEntity)
          .map(e -> (E) new FileMeta(e))
          .collect(toList());
    } else {
      throw new UnsupportedOperationException("Can't return typed pretending entities");
    }
  }

  private PretendingEntity newPretendingEntity(Entity entity) {
    return new PretendingEntity(entity, copiedEntityTypes);
  }
}

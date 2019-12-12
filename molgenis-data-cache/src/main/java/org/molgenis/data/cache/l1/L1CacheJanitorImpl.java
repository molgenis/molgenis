package org.molgenis.data.cache.l1;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.cache.utils.CacheHit;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityUtils;
import org.springframework.stereotype.Component;

@Component
public class L1CacheJanitorImpl implements L1CacheJanitor {
  private final L1Cache l1Cache;

  public L1CacheJanitorImpl(L1Cache l1Cache) {
    this.l1Cache = requireNonNull(l1Cache);
  }

  @Override
  public void cleanCacheBeforeAdd(Entity entity) {
    cleanCacheBeforeAddOrDelete(entity);
  }

  @Override
  public Stream<Entity> cleanCacheBeforeAdd(EntityType entityType, Stream<Entity> entityStream) {
    return cleanCacheBeforeAddOrDelete(entityType, entityStream);
  }

  private void cleanCacheBeforeAddOrDelete(Attribute attribute, Entity entity) {
    Entity refEntity = entity.getEntity(attribute);
    if (refEntity != null && !EntityUtils.isSame(entity, refEntity)) {
      l1Cache.evict(refEntity);
    }
  }

  @Override
  public void cleanCacheBeforeUpdate(Entity entity) {
    EntityType entityType = entity.getEntityType();
    Iterator<Attribute> iterator = entityType.getInversedByAttributes().iterator();
    if (!iterator.hasNext()) {
      return;
    }

    Entity currentEntity =
        l1Cache.get(entityType, entity.getIdValue()).map(CacheHit::getValue).orElse(null);

    do {
      cleanCacheBeforeUpdate(iterator.next(), entity, currentEntity);
    } while (iterator.hasNext());
  }

  @Override
  public Stream<Entity> cleanCacheBeforeUpdate(EntityType entityType, Stream<Entity> entityStream) {
    List<Attribute> attributes = entityType.getInversedByAttributes().collect(toList());
    if (attributes.isEmpty()) {
      return entityStream;
    }

    return entityStream.filter(
        entity -> {
          Entity currentEntity =
              l1Cache.get(entityType, entity.getIdValue()).map(CacheHit::getValue).orElse(null);

          attributes.forEach(attribute -> cleanCacheBeforeUpdate(attribute, entity, currentEntity));
          return true;
        });
  }

  private void cleanCacheBeforeUpdate(
      Attribute attribute, Entity updatedEntity, @Nullable @CheckForNull Entity currentEntity) {
    if (currentEntity == null) {
      l1Cache.evictAll(attribute.getRefEntity());
    } else {
      Entity currentRefEntity = currentEntity.getEntity(attribute);
      Entity updatedRefEntity = updatedEntity.getEntity(attribute);
      if (currentRefEntity == null) {
        if (updatedRefEntity != null && !EntityUtils.isSame(updatedEntity, updatedRefEntity)) {
          l1Cache.evict(updatedRefEntity);
        }
      } else {
        cleanCacheBeforeUpdateRefEntity(updatedEntity, currentRefEntity, updatedRefEntity);
      }
    }
  }

  private void cleanCacheBeforeUpdateRefEntity(
      Entity updatedEntity, Entity currentRefEntity, Entity updatedRefEntity) {
    if (updatedRefEntity == null) {
      if (!EntityUtils.isSame(updatedEntity, currentRefEntity)) {
        l1Cache.evict(currentRefEntity);
      }
    } else {
      if (!EntityUtils.isSame(currentRefEntity, updatedRefEntity)) {
        if (!EntityUtils.isSame(updatedEntity, updatedRefEntity)) {
          l1Cache.evict(updatedRefEntity);
        }
        if (!EntityUtils.isSame(updatedEntity, currentRefEntity)) {
          l1Cache.evict(currentRefEntity);
        }
      }
    }
  }

  @Override
  public void cleanCacheBeforeDelete(Entity entity) {
    cleanCacheBeforeAddOrDelete(entity);
  }

  @Override
  public Stream<Entity> cleanCacheBeforeDelete(EntityType entityType, Stream<Entity> entityStream) {
    return cleanCacheBeforeAddOrDelete(entityType, entityStream);
  }

  @Override
  public void cleanCacheBeforeDeleteById(EntityType entityType, Object entityId) {
    Iterator<Attribute> iterator = entityType.getInversedByAttributes().iterator();
    if (!iterator.hasNext()) {
      return;
    }

    Entity entity = l1Cache.get(entityType, entityId).map(CacheHit::getValue).orElse(null);

    do {
      cleanCacheBeforeDeleteById(iterator.next(), entity);
    } while (iterator.hasNext());
  }

  @Override
  public Stream<Object> cleanCacheBeforeDeleteById(
      EntityType entityType, Stream<Object> entityIdStream) {
    List<Attribute> attributes = entityType.getInversedByAttributes().collect(toList());
    if (attributes.isEmpty()) {
      return entityIdStream;
    }

    return entityIdStream.filter(
        entityId -> {
          Entity entity = l1Cache.get(entityType, entityId).map(CacheHit::getValue).orElse(null);
          attributes.forEach(attribute -> cleanCacheBeforeDeleteById(attribute, entity));
          return true;
        });
  }

  private void cleanCacheBeforeDeleteById(
      Attribute attribute, @Nullable @CheckForNull Entity entity) {
    if (entity == null) {
      l1Cache.evictAll(attribute.getRefEntity());
    } else {
      cleanCacheBeforeAddOrDelete(attribute, entity);
    }
  }

  @Override
  public void cleanCacheBeforeDeleteAll(EntityType entityType) {
    // no cleanup required
  }

  private void cleanCacheBeforeAddOrDelete(Entity entity) {
    Iterator<Attribute> iterator = entity.getEntityType().getInversedByAttributes().iterator();
    if (!iterator.hasNext()) {
      return;
    }

    do {
      cleanCacheBeforeAddOrDelete(iterator.next(), entity);
    } while (iterator.hasNext());
  }

  private Stream<Entity> cleanCacheBeforeAddOrDelete(
      EntityType entityType, Stream<Entity> entityStream) {
    List<Attribute> attributes = entityType.getInversedByAttributes().collect(toList());
    if (attributes.isEmpty()) {
      return entityStream;
    }

    return entityStream.filter(
        entity -> {
          attributes.forEach(attribute -> cleanCacheBeforeAddOrDelete(attribute, entity));
          return true;
        });
  }
}

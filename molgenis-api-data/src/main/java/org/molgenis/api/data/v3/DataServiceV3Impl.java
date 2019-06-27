package org.molgenis.api.data.v3;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.data.v3.Selection.EMPTY_SELECTION;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownRepositoryException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.data.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class DataServiceV3Impl implements DataServiceV3 {
  private final MetaDataService metaDataService;

  DataServiceV3Impl(MetaDataService metaDataService) {
    this.metaDataService = requireNonNull(metaDataService);
  }

  @Transactional(readOnly = true)
  @Override
  public Entity find(String entityTypeId, String entityId, Selection filter, Selection expand) {
    Repository<Entity> repository =
        metaDataService
            .getRepository(entityTypeId)
            .orElseThrow(() -> new UnknownRepositoryException(entityTypeId));
    EntityType entityType = repository.getEntityType();

    Object typedEntityId = toTypedEntityId(entityType, entityId);
    Fetch fetch = toFetch(entityType, filter, expand);

    Entity entity = repository.findOneById(typedEntityId, fetch);
    if (entity == null) {
      throw new UnknownEntityException(entityTypeId, entityId);
    }

    return entity;
  }

  private Object toTypedEntityId(EntityType entityType, String entityId) {
    Attribute idAttribute = entityType.getIdAttribute();
    return EntityUtils.getTypedValue(entityId, idAttribute);
  }

  private @CheckForNull @Nullable Fetch toFetch(
      EntityType entityType, Selection filter, Selection expand) {
    if (!filter.hasItems()) {
      return null;
    }

    Fetch fetch = new Fetch();

    Iterable<Attribute> attributes = entityType.getAtomicAttributes();
    attributes.forEach(
        attribute -> {
          String attributeName = attribute.getName();

          if (filter.hasItem(attributeName)) {
            Fetch subFetch;
            if (expand.hasItem(attributeName) && EntityTypeUtils.isReferenceType(attribute)) {
              Selection subFilter = filter.getSelection(attributeName).orElse(EMPTY_SELECTION);
              Selection subExpand = expand.getSelection(attributeName).orElse(EMPTY_SELECTION);
              subFetch = toFetch(attribute.getRefEntity(), subFilter, subExpand);
            } else {
              subFetch = null;
            }

            fetch.field(attributeName, subFetch);
          }
        });

    return fetch;
  }
}

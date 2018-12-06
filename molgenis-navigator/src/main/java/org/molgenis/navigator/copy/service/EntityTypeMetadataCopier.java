package org.molgenis.navigator.copy.service;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS;

import java.util.Map;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

@Component
public class EntityTypeMetadataCopier {

  private final AttributeFactory attributeFactory;

  EntityTypeMetadataCopier(AttributeFactory attributeFactory) {
    this.attributeFactory = requireNonNull(attributeFactory);
  }

  public EntityType copy(EntityType entityType, CopyState state) {
    EntityType copy = EntityType.newInstance(entityType, SHALLOW_COPY_ATTRS, attributeFactory);
    Map<String, Attribute> copiedAttributes =
        EntityType.deepCopyAttributes(entityType, copy, attributeFactory);
    state.copiedAttributes().putAll(copiedAttributes);
    return copy;
  }
}

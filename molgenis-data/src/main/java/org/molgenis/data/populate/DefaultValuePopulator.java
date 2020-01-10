package org.molgenis.data.populate;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityReferenceCreator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.util.AttributeUtils;
import org.springframework.stereotype.Component;

/** Populate entity values for attributes with default values */
@Component
public class DefaultValuePopulator {

  private final EntityReferenceCreator entityReferenceCreator;

  public DefaultValuePopulator(EntityReferenceCreator entityReferenceCreator) {
    this.entityReferenceCreator = requireNonNull(entityReferenceCreator);
  }

  /**
   * Populates an entity with default values
   *
   * @param entity populated entity
   */
  public void populate(Entity entity) {
    stream(entity.getEntityType().getAllAttributes())
        .filter(Attribute::hasDefaultValue)
        .forEach(attr -> populateDefaultValues(entity, attr));
  }

  private void populateDefaultValues(Entity entity, Attribute attr) {
    Object defaultValueAsString = getDefaultValue(attr);
    entity.set(attr.getName(), defaultValueAsString);
  }

  private Object getDefaultValue(Attribute attr) {
    return AttributeUtils.getDefaultTypedValue(attr, entityReferenceCreator);
  }
}

package org.molgenis.data.meta;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.AttributeMetadata.MAPPED_BY;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ATTRIBUTES;

import java.time.Instant;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;

/**
 * Entity meta data that wraps a entity meta data and hides the mappedBy attributes. In code both a
 * new and an existing entity meta data are provided only the new mappedBy attributes are hidden.
 */
public class EntityTypeWithoutMappedByAttributes extends EntityType {
  private final EntityType entityType;
  private final EntityType existingEntityType;

  public EntityTypeWithoutMappedByAttributes(EntityType entityType) {
    this(entityType, null);
  }

  public EntityTypeWithoutMappedByAttributes(EntityType entityType, EntityType existingEntityType) {
    this.entityType = requireNonNull(entityType);
    this.existingEntityType = existingEntityType;
  }

  @Override
  public void init(Entity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object get(String attributeName) {
    return entityType.get(attributeName);
  }

  @Override
  public Boolean getBoolean(String attributeName) {
    return entityType.getBoolean(attributeName);
  }

  @Override
  public Double getDouble(String attributeName) {
    return entityType.getDouble(attributeName);
  }

  @Override
  public Iterable<Entity> getEntities(String attributeName) {
    Iterable<Entity> entities = entityType.getEntities(attributeName);
    if (attributeName.equals(ATTRIBUTES)) {
      return () ->
          stream(entities)
              .filter(
                  entity -> {
                    if (existingEntityType != null) {
                      return entity.getEntity(MAPPED_BY) == null
                          || existingEntityType.getAttribute(
                                  entity.getString(AttributeMetadata.NAME))
                              != null;
                    } else {
                      return entity.getEntity(MAPPED_BY) == null;
                    }
                  })
              .iterator();
    }
    return entities;
  }

  @Override
  public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz) {
    return entityType.getEntities(attributeName, clazz);
  }

  @Override
  public Entity getEntity(String attributeName) {
    return entityType.getEntity(attributeName);
  }

  @Override
  public <E extends Entity> E getEntity(String attributeName, Class<E> clazz) {
    return entityType.getEntity(attributeName, clazz);
  }

  @Override
  public EntityType getEntityType() {
    return entityType.getEntityType();
  }

  @Override
  public Object getIdValue() {
    return entityType.getIdValue();
  }

  @Override
  public Integer getInt(String attributeName) {
    return entityType.getInt(attributeName);
  }

  @Override
  public Object getLabelValue() {
    return entityType.getLabelValue();
  }

  @Override
  public Long getLong(String attributeName) {
    return entityType.getLong(attributeName);
  }

  @Override
  public String getString(String attributeName) {
    return entityType.getString(attributeName);
  }

  @Override
  public LocalDate getLocalDate(String attributeName) {
    return entityType.getLocalDate(attributeName);
  }

  @Override
  public Instant getInstant(String attributeName) {
    return entityType.getInstant(attributeName);
  }

  @Override
  public void set(Entity values) {
    entityType.set(values);
  }

  @Override
  public void setIdValue(Object id) {
    entityType.setIdValue(id);
  }

  @Override
  public Iterable<String> getAttributeNames() {
    return entityType.getAttributeNames();
  }

  @Override
  public String getId() {
    return entityType.getId();
  }

  @Override
  public EntityType setId(String id) {
    return entityType.setId(id);
  }

  @Override
  public String getLabel() {
    return entityType.getLabel();
  }

  @Override
  public String getLabel(String languageCode) {
    return entityType.getLabel(languageCode);
  }

  @Override
  public EntityType setLabel(String label) {
    return entityType.setLabel(label);
  }

  @Override
  public EntityType setLabel(String languageCode, String label) {
    return entityType.setLabel(languageCode, label);
  }

  @Override
  public String getDescription() {
    return entityType.getDescription();
  }

  @Override
  public String getDescription(String languageCode) {
    return entityType.getDescription(languageCode);
  }

  @Override
  public EntityType setDescription(String description) {
    return entityType.setDescription(description);
  }

  @Override
  public EntityType setDescription(String languageCode, String description) {
    return entityType.setDescription(languageCode, description);
  }

  @Override
  public String getBackend() {
    return entityType.getBackend();
  }

  @Override
  public EntityType setBackend(String backend) {
    return entityType.setBackend(backend);
  }

  @Override
  public Package getPackage() {
    return entityType.getPackage();
  }

  @Override
  public EntityType setPackage(Package aPackage) {
    return entityType.setPackage(aPackage);
  }

  @Override
  public Attribute getIdAttribute() {
    return entityType.getIdAttribute();
  }

  @Override
  public Attribute getOwnIdAttribute() {
    return entityType.getOwnIdAttribute();
  }

  @Override
  public Attribute getLabelAttribute() {
    return entityType.getLabelAttribute();
  }

  @Override
  public Attribute getLabelAttribute(String langCode) {
    return entityType.getLabelAttribute(langCode);
  }

  @Override
  public Attribute getOwnLabelAttribute() {
    return entityType.getOwnLabelAttribute();
  }

  @Override
  public Attribute getOwnLabelAttribute(String languageCode) {
    return entityType.getOwnLabelAttribute(languageCode);
  }

  @Override
  public Attribute getLookupAttribute(String lookupAttrName) {
    return entityType.getLookupAttribute(lookupAttrName);
  }

  @Override
  public Iterable<Attribute> getLookupAttributes() {
    return entityType.getLookupAttributes();
  }

  @Override
  public Iterable<Attribute> getOwnLookupAttributes() {
    return entityType.getOwnLookupAttributes();
  }

  @Override
  public boolean isAbstract() {
    return entityType.isAbstract();
  }

  @Override
  public EntityType setAbstract(boolean isAbstract) {
    return entityType.setAbstract(isAbstract);
  }

  @Override
  public EntityType getExtends() {
    return entityType.getExtends();
  }

  @Override
  public EntityType setExtends(EntityType extendsEntityType) {
    return entityType.setExtends(extendsEntityType);
  }

  @Override
  public Iterable<Attribute> getOwnAttributes() {
    // FIXME mappedBy attribute in compound not removed
    return () ->
        stream(entityType.getOwnAttributes())
            .filter(
                attr -> {
                  if (existingEntityType != null) {
                    return !attr.isMappedBy()
                        || existingEntityType.getAttribute(attr.getName()) != null;
                  } else {
                    return !attr.isMappedBy();
                  }
                })
            .iterator();
  }

  @Override
  public EntityType setOwnAllAttributes(Iterable<Attribute> attrs) {
    return entityType.setOwnAllAttributes(attrs);
  }

  @Override
  public Iterable<Attribute> getAttributes() {
    return entityType.getAttributes();
  }

  @Override
  public Iterable<Attribute> getAtomicAttributes() {
    return entityType.getAtomicAttributes();
  }

  @Override
  public Iterable<Attribute> getAllAttributes() {
    return entityType.getAllAttributes();
  }

  @Override
  public Iterable<Attribute> getOwnAllAttributes() {
    return () ->
        stream(entityType.getOwnAllAttributes())
            .filter(
                attr -> {
                  if (existingEntityType != null) {
                    return !attr.isMappedBy()
                        || existingEntityType.getAttribute(attr.getName()) != null;
                  } else {
                    return !attr.isMappedBy();
                  }
                })
            .iterator();
  }

  @Override
  public Attribute getAttribute(String attrName) {
    return entityType.getAttribute(attrName);
  }

  @Override
  public EntityType addAttribute(Attribute attr, AttributeRole... attrTypes) {
    return entityType.addAttribute(attr, attrTypes);
  }

  @Override
  public void addAttributes(Iterable<Attribute> attrs) {
    entityType.addAttributes(attrs);
  }

  @Override
  public void setAttributeRoles(Attribute attr, AttributeRole... attrTypes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasAttributeWithExpression() {
    return entityType.hasAttributeWithExpression();
  }

  @Override
  public void removeAttribute(Attribute attr) {
    entityType.removeAttribute(attr);
  }

  @Override
  public Iterable<Tag> getTags() {
    return entityType.getTags();
  }

  @Override
  public EntityType setTags(Iterable<Tag> tags) {
    return entityType.setTags(tags);
  }

  @Override
  public void addTag(Tag tag) {
    entityType.addTag(tag);
  }

  @Override
  public void removeTag(Tag tag) {
    entityType.removeTag(tag);
  }

  @Override
  public Iterable<Attribute> getOwnAtomicAttributes() {
    return entityType.getOwnAtomicAttributes();
  }

  @Override
  public boolean hasBidirectionalAttributes() {
    return entityType.hasBidirectionalAttributes();
  }

  @Override
  public boolean hasMappedByAttributes() {
    return entityType.hasMappedByAttributes();
  }

  @Override
  public Stream<Attribute> getOwnMappedByAttributes() {
    return entityType.getOwnMappedByAttributes();
  }

  @Override
  public Stream<Attribute> getMappedByAttributes() {
    return entityType.getMappedByAttributes();
  }

  @Override
  public boolean hasInversedByAttributes() {
    return entityType.hasInversedByAttributes();
  }

  @Override
  public Stream<Attribute> getInversedByAttributes() {
    return entityType.getInversedByAttributes();
  }

  @Override
  public void set(String attributeName, Object value) {
    entityType.set(attributeName, value);
  }

  @Override
  public void setDefaultValues() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return entityType.toString();
  }
}

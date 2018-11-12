package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Streams.stream;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;

import java.util.Map;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.util.EntityTypeUtils;
import org.springframework.stereotype.Service;

/**
 * Will transform relations of an {@link EntityType} (references, abstract relations and packages)
 * to a set of new relations, leaving old relations intact if no new relations are supplied.
 */
@Service
class RelationTransformer {

  private RelationTransformer() {}

  /**
   * Changes the {@link Package} of an {@link EntityType} to another Package if it was present in
   * the supplied Map. Does nothing if the Map does not contain the ID of the current Package.
   *
   * @param entityType the EntityType to update
   * @param newPackages a Map of (old) Package IDs and new Packages
   */
  static void transformPackage(EntityType entityType, Map<String, Package> newPackages) {
    if (newPackages.isEmpty()) {
      return;
    }

    if (entityType.getPackage() != null) {
      String packageId = entityType.getPackage().getId();
      if (newPackages.containsKey(packageId)) {
        entityType.setPackage(newPackages.get(packageId));
      }
    }
  }

  /**
   * Changes the parent of an {@link EntityType} to let it inherit from another, new EntityType.
   * Does nothing if the Map does not contain the ID of the current parent EntityType.
   *
   * @param entityType the EntityType to update
   * @param newEntityTypes a Map of (old) EntityType IDs and new EntityTypes
   */
  static void transformExtends(EntityType entityType, Map<String, EntityType> newEntityTypes) {
    if (newEntityTypes.isEmpty()) {
      return;
    }

    if (entityType.getExtends() != null) {
      String extendsId = entityType.getExtends().getId();
      if (newEntityTypes.containsKey(extendsId)) {
        entityType.setExtends(newEntityTypes.get(extendsId));
      }
    }
  }

  /**
   * Changes all references of an {@link EntityType} to point to other, new EntityTypes. Does
   * nothing for references whose IDs are not present in the supplied Map.
   *
   * @param entityType the EntityType to update
   * @param newEntityTypes a Map of (old) EntityType IDs and new EntityTypes
   */
  static void transformRefEntities(EntityType entityType, Map<String, EntityType> newEntityTypes) {
    if (newEntityTypes.isEmpty()) {
      return;
    }

    stream(entityType.getAtomicAttributes())
        .filter(EntityTypeUtils::isReferenceType)
        .forEach(attr -> transformRefEntity(attr, newEntityTypes));
  }

  private static void transformRefEntity(
      Attribute attribute, Map<String, EntityType> newEntityTypes) {
    if (attribute.getRefEntity() != null) {
      String refId = attribute.getRefEntity().getId();
      if (newEntityTypes.containsKey(refId)) {
        attribute.setRefEntity(newEntityTypes.get(refId));
      }
    }
  }

  /**
   * Changes the 'mappedBy' property of all {@link AttributeType#ONE_TO_MANY} attributes of an
   * {@link EntityType} to other, new Attributes. Does nothing for mappedBy attributes whose IDs are
   * not present in the supplied Map.
   *
   * @param entityType the EntityType to update
   * @param newAttributes a Map of (old) Attribute IDs and new Attributes
   */
  static void transformMappedBys(EntityType entityType, Map<String, Attribute> newAttributes) {
    if (newAttributes.isEmpty()) {
      return;
    }

    stream(entityType.getAtomicAttributes())
        .filter(attr -> attr.getDataType() == ONE_TO_MANY)
        .forEach(attr -> transformMappedBy(attr, newAttributes));
  }

  private static void transformMappedBy(Attribute attribute, Map<String, Attribute> newAttributes) {
    if (attribute.getMappedBy() != null) {
      String mappedByAttrId = attribute.getMappedBy().getIdentifier();
      if (newAttributes.containsKey(mappedByAttrId)) {
        attribute.setMappedBy(newAttributes.get(mappedByAttrId));
      }
    }
  }
}

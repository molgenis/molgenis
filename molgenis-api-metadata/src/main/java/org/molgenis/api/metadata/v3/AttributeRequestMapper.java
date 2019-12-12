package org.molgenis.api.metadata.v3;

import java.util.List;
import java.util.Map;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

public interface AttributeRequestMapper {
  /**
   * Creates a new attribute from an attribute request.
   *
   * @param attributeRequest attribute request
   * @param entityType entity type that this attribute will belong to
   * @return new attribute
   */
  Attribute toAttribute(CreateAttributeRequest attributeRequest, EntityType entityType);

  /**
   * Updates an attribute in-place.
   *
   * @param attribute attribute to update
   * @param attributeValues attribute values to apply
   */
  void updateAttribute(Attribute attribute, Map<String, Object> attributeValues);

  List<Attribute> toAttributes(
      List<CreateAttributeRequest> attributes,
      CreateEntityTypeRequest entityTypeRequest,
      EntityType entityType);

  Map<String, Attribute> toAttributes(
      List<Map<String, Object>> attributeValueMaps, EntityType entityType);
}

package org.molgenis.api.metadata.v3;

import java.util.List;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.data.meta.model.Attribute;

public interface AttributeResponseMapper {
  /**
   * Creates an attribute response from an attribute.
   *
   * @param attribute attribute to map
   * @param i18n whether to include localized labels and descriptions
   * @return attribute response
   */
  AttributeResponse toAttributeResponse(Attribute attribute, boolean i18n);

  /**
   * Creates an attributes response from attributes.
   *
   * @param attributes attributes to map
   * @param size number of entity types
   * @param page page number
   * @return attributes response
   */
  AttributesResponse toAttributesResponse(Attributes attributes, int size, int page);

  List<AttributeResponse> mapInternal(Iterable<Attribute> allAttributes, boolean i18n);
}

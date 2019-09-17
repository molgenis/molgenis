package org.molgenis.api.meta.model;

import java.util.List;

public class CreateEntityTypeRequest {
  String id;
  I18nValue label;
  I18nValue description;
  Boolean isAbstract;
  String aPackage;
  String entityTypeParent;
  List<CreateAttributeRequest> attributes;
  String idAttribute;
  String labelAttribute;
  List<String> lookupAttributes;

  public CreateEntityTypeRequest(
      String id,
      I18nValue label,
      I18nValue description,
      Boolean isAbstract,
      String aPackage,
      String entityTypeParent,
      List<CreateAttributeRequest> attributes,
      String idAttribute,
      String labelAttribute,
      List<String> lookupAttributes) {
    this.id = id;
    this.label = label;
    this.description = description;
    this.isAbstract = isAbstract;
    this.aPackage = aPackage;
    this.entityTypeParent = entityTypeParent;
    this.attributes = attributes;
    this.idAttribute = idAttribute;
    this.labelAttribute = labelAttribute;
    this.lookupAttributes = lookupAttributes;
  }

  public String getId() {
    return id;
  }

  public I18nValue getLabel() {
    return label;
  }

  public I18nValue getDescription() {
    return description;
  }

  public Boolean isAbstract() {
    return isAbstract;
  }

  public String getPackage() {
    return aPackage;
  }

  public String getEntityTypeParent() {
    return entityTypeParent;
  }

  public List<CreateAttributeRequest> getAttributes() {
    return attributes;
  }

  public String getIdAttribute() {
    return idAttribute;
  }

  public String getLabelAttribute() {
    return labelAttribute;
  }

  public List<String> getLookupAttributes() {
    return lookupAttributes;
  }
}

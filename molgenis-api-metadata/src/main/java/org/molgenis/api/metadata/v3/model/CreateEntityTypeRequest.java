package org.molgenis.api.metadata.v3.model;

import static java.util.Collections.emptyList;

import java.util.List;

public class CreateEntityTypeRequest {
  String id;
  I18nValue label;
  I18nValue description;
  Boolean abstract_;
  String package_;
  String extends_;
  List<CreateAttributeRequest> attributes;
  String idAttribute;
  String labelAttribute;
  List<String> lookupAttributes;

  public CreateEntityTypeRequest(
      String id,
      I18nValue label,
      I18nValue description,
      Boolean abstract_,
      String package_,
      String extends_,
      List<CreateAttributeRequest> attributes,
      String idAttribute,
      String labelAttribute,
      List<String> lookupAttributes) {
    this.id = id;
    this.label = label;
    this.description = description;
    this.abstract_ = abstract_;
    this.package_ = package_;
    this.extends_ = extends_;
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

  public boolean isAbstract() {
    return abstract_ != null ? abstract_ : false;
  }

  public String getPackage() {
    return package_;
  }

  public String getExtends() {
    return extends_;
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
    return lookupAttributes != null ? lookupAttributes : emptyList();
  }
}

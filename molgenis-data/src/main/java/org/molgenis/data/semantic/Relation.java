package org.molgenis.data.semantic;

public enum Relation {
  INSTANCE_OF("http://molgenis.org/biobankconnect/instanceOf"),
  LINK("http://molgenis.org/biobankconnect/link"),
  HOMEPAGE("http://xmlns.com/foaf/0.1/homepage"),
  IS_DEFINED_BY("http://www.w3.org/2000/01/rdf-schema#isDefinedBy"),
  SEE_ALSO("http://www.w3.org/2000/01/rdf-schema#seeAlso"),
  HAS_LOWER_VALUE("http://molgenis.org/uml/hasLowerValue"),
  HAS_UPPER_VALUE("http://molgenis.org/uml/hasUpperValue"),
  IS_RELATION_OF("http://molgenis.org/uml/isRealizationOf"),
  IS_GENERALIZATION_OF("http://molgenis.org/uml/isGeneralizationOf"),
  HAS_RESOURCE_ID("http://molgenis.org/uml/hasSourceId"),
  HAS_SOURCE_NAME("http://molgenis.org/uml/hasSourceName"),
  IS_ASSOCIATED_WITH("http://molgenis.org#isAssociatedWith");

  private String iri;

  Relation(String iri) {
    this.iri = iri;
  }

  public String getIRI() {
    return iri;
  }

  public String getLabel() {
    return toString();
  }

  public static Relation forIRI(String string) {
    for (Relation relation : values()) {
      if (relation.getIRI().equals(string)) {
        return relation;
      }
    }
    return null;
  }
}

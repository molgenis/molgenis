package org.molgenis.data.semantic;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

@SuppressWarnings("java:S115") // Constant names should comply with a naming convention
public enum Relation {
  instanceOf("http://molgenis.org/biobankconnect/instanceOf"),
  link("http://molgenis.org/biobankconnect/link"),
  homepage(FOAF.HOMEPAGE),
  type(RDF.TYPE),
  isDefinedBy(RDFS.ISDEFINEDBY),
  seeAlso(RDFS.SEEALSO),
  hasLowerValue("http://molgenis.org/uml/hasLowerValue"),
  hasUpperValue("http://molgenis.org/uml/hasUpperValue"),
  isRealizationOf("http://molgenis.org/uml/isRealizationOf"),
  isGeneralizationOf("http://molgenis.org/uml/isGeneralizationOf"),
  hasSourceId("http://molgenis.org/uml/hasSourceId"),
  hasSourceName("http://molgenis.org/uml/hasSourceName"),
  isAssociatedWith("http://molgenis.org#isAssociatedWith"),
  isAudited("http://molgenis.org/audit#isAudited");

  private String iri;

  Relation(IRI iri) {
    this.iri = iri.toString();
  }

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

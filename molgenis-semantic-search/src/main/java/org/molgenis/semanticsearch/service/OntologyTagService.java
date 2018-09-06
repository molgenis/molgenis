package org.molgenis.semanticsearch.service;

import java.util.List;
import java.util.Map;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.semanticsearch.semantic.OntologyTag;

public interface OntologyTagService extends TagService<OntologyTerm, Ontology> {
  OntologyTag addAttributeTag(
      String entityTypeId, String attributeName, String relationIRI, List<String> ontologyTermIRIs);

  void removeAttributeTag(
      String entityTypeId, String attributeName, String relationIRI, String ontologyTermIRI);

  Map<String, OntologyTag> tagAttributesInEntity(String entity, Map<Attribute, OntologyTerm> tags);
}

package org.molgenis.data.semanticsearch.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.semanticsearch.semantic.OntologyTag;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;

import java.util.List;
import java.util.Map;

public interface OntologyTagService extends TagService<OntologyTerm, Ontology>
{

	OntologyTag addAttributeTag(String entityName, String attributeName, String relationIRI,
			List<String> ontologyTermIRIs);

	void removeAttributeTag(String entityName, String attributeName, String relationIRI, String ontologyTermIRI);

	Map<String, OntologyTag> tagAttributesInEntity(String entity, Map<Attribute, OntologyTerm> tags);
}

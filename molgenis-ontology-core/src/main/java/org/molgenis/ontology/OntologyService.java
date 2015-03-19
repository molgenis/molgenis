package org.molgenis.ontology;

import org.molgenis.ontology.repository.model.Ontology;
import org.molgenis.ontology.repository.model.OntologyTerm;

public interface OntologyService
{

	Ontology getOntology(String name);

	OntologyTerm getOntologyTerm(String ontology, String iri);

}

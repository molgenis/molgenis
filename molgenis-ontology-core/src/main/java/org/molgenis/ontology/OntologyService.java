package org.molgenis.ontology;

import java.util.List;

import org.molgenis.ontology.repository.model.Ontology;
import org.molgenis.ontology.repository.model.OntologyTerm;

public interface OntologyService
{
	List<Ontology> getOntologies();

	Ontology getOntology(String name);

	List<OntologyTerm> findOntologyTerms(List<Ontology> ontologies, String search);

	// voor de tag service
	OntologyTerm getOntologyTerm(String ontology, String iri);

}

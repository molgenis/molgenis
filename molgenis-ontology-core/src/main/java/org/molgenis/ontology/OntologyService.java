package org.molgenis.ontology;

import java.util.List;

import org.molgenis.ontology.repository.model.Ontology;
import org.molgenis.ontology.repository.model.OntologyTerm;

public interface OntologyService
{
	List<Ontology> getOntologies();

	Ontology getOntology(String name);

	OntologyTerm findOntologyTerm(List<Ontology> ontologies, String search);

	// voor de tag service
	OntologyTerm getOntologyTerm(String ontology, String iri);

}

package org.molgenis.ontology.beans;

import java.util.Set;

public interface OntologyTerm
{
	String getIRI();

	String getName();

	Set<String> getSynonyms();

	Ontology getOntology();
}

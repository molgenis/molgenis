package org.molgenis.ontology;

import java.util.Set;

public interface OntologyTerm
{
	String getIRI();

	String getLabel();

	String getDescription();

	/**
	 * @return Computer readable ID for the term, unique within the ontology.
	 */
	String getTermAccession();

	/**
	 * @return Labels of synonyms
	 */
	Set<String> getSynonyms();

	Ontology getOntology();
}

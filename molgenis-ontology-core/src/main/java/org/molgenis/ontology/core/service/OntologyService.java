package org.molgenis.ontology.core.service;

import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;

import java.util.List;
import java.util.Set;

public interface OntologyService
{
	/**
	 * Retrieves all ontologies.
	 */
	List<Ontology> getOntologies();

	/**
	 * Retrieves a specific ontology
	 *
	 * @param iri IRI of the ontology to retrieve.
	 * @return the Ontology
	 */
	Ontology getOntology(String iri);

	/**
	 * Finds ontology terms that are exact matches to a certain search string.
	 *
	 * @param pageSize number of results to return.
	 * @return List of {@link OntologyTerm}s that match the search term.
	 */
	List<OntologyTerm> findExcatOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

	/**
	 * Finds ontology terms that match a certain search string.
	 *
	 * @param pageSize number of results to return.
	 * @return List of {@link OntologyTerm}s that match the search term.
	 */
	List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

	/**
	 * Retrieve all ontology terms from the specified ontology
	 *
	 * @param ontologyIri
	 * @return all the {@link OntologyTerm}
	 */
	List<OntologyTerm> getAllOntologyTerms(String ontologyIri);

	// voor de tag service

	/**
	 * Retrieves a specific OntologyTerm
	 *
	 * @return Combined {@link OntologyTerm} for all IRI's listed
	 */
	OntologyTerm getOntologyTerm(String iri);

	/**
	 * Retrieves all children from the current ontology term
	 *
	 * @param ontologyTerm
	 * @return a list of {@link OntologyTerm} as children
	 */
	List<OntologyTerm> getChildren(OntologyTerm ontologyTerm);

	/**
	 * Calculate distance between two ontology terms
	 *
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	Integer getOntologyTermDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2);

	/**
	 * Retrieves all ontologies ids.
	 *
	 * @return String Ontology Id
	 */
	List<String> getAllOntologiesIds();
}

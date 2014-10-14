package org.molgenis.data.semantic;

import org.molgenis.data.Entity;

/**
 * Makes Ontology trees manageable
 * 
 * @author chaopang
 * 
 */
public interface OntologyService
{
	// TODO : to be added in the future for importing ontology
	// void importOntology();

	/**
	 * 
	 * @return a list of all available ontologies
	 */
	Iterable<Ontology> getAllOntologies();

	/**
	 * 
	 * @param ontologyIri
	 * @return an ontology based on the ontologyIri
	 */
	Ontology getOntology(String ontologyIri);

	/**
	 * 
	 * @param queryTerm
	 * @param ontologyIri
	 * @return a list of ontology terms sorted based on the relevance
	 */
	Iterable<OntologyTerm> findOntologyTerms(String queryTerm, String ontologyIri);

	/**
	 * 
	 * @param ontologyTermIRI
	 * @param ontologyIri
	 * @return an ontologyTerm based on specified ontologyTermIri in particular
	 *         ontology
	 */
	OntologyTerm getOntologyTerm(String ontologyTermIri, String ontologyIri);

	/**
	 * 
	 * @param ontologyIri
	 * @return a list of ontology terms from specified ontology
	 * @throws NullPointerException
	 *             if the ontologyIri is not known
	 */
	Iterable<OntologyTerm> getAllOntologyTerms(String ontologyIri);

	/**
	 * 
	 * @param ontologyIri
	 * @return a list of root ontology terms from specified ontology
	 * @throws NullPointerException
	 *             if the ontologyIri is not known
	 */
	Iterable<OntologyTerm> getRootOntologyTerms(String ontologyIri);

	/**
	 * 
	 * @param ontologyIri
	 * @param ontologyTerm
	 * @return a list of child ontologyTerms from current ontologyTerm in
	 *         specified ontology
	 * @throws NullPointerException
	 *             if the ontologyIri is not known
	 * @throws NullPointerException
	 *             if the ontologyTermIri is not known
	 */
	Iterable<OntologyTerm> getChildOntologyTerms(String ontologyIri, String ontologyTermIri);

	/**
	 * 
	 * @param ontologyIri
	 * @param entity
	 *            input for storing the query information
	 * @return a list of relevant ontologyTerms based on the given input stored
	 *         in an entity
	 * @throws NullPointerException
	 *             if the ontologyIri is not known
	 */
	OntologyServiceResult searchEntity(String ontologyIri, Entity entity);

	/**
	 * 
	 * @param ontologyUrl
	 * @param queryString
	 * @return a list of relevant ontologyTerms based on give queryString
	 */
	OntologyServiceResult search(String ontologyUrl, String queryString);
}

package org.molgenis.ontology;

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
	 * @return a list of untyped ontology entities
	 */
	Iterable<Entity> getAllOntologyEntities();

	/**
	 * 
	 * @param ontologyIri
	 * @return an ontology based on the ontologyIri
	 */
	Ontology getOntology(String ontologyIri);

	/**
	 * 
	 * @param ontologyIri
	 * @return an untyped ontology based on the ontologyIri
	 */
	Entity getOntologyEntity(String ontologyIri);

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
	 * @param ontologyTermIri
	 * @param ontologyIri
	 * @return an untyped ontologyTerm entity based on specified ontologyTermIri
	 *         in particular ontology
	 */
	Entity getOntologyTermEntity(String ontologyTermIri, String ontologyIri);

	/**
	 * 
	 * @param ontologyIri
	 * @return a list of ontologyterms from specified ontology
	 * @throws NullPointerException
	 *             if the ontologyIri is not known
	 */
	Iterable<OntologyTerm> getAllOntologyTerms(String ontologyIri);

	/**
	 * 
	 * @param ontologyIri
	 * @return a list of untyped ontologyterm entities from specified ontology
	 */
	Iterable<Entity> getAllOntologyTermEntities(String ontologyIri);

	/**
	 * 
	 * @param ontologyIri
	 * @return a list of root ontologyterms from specified ontology
	 * @throws NullPointerException
	 *             if the ontologyIri is not known
	 */
	Iterable<OntologyTerm> getRootOntologyTerms(String ontologyIri);

	/**
	 * 
	 * @param ontologyIri
	 * @return a list of untyped root ontologyterm entities from specified
	 *         ontology
	 * @throws NullPointerException
	 *             if the ontologyIri is not known
	 */
	Iterable<Entity> getRootOntologyTermEntities(String ontologyIri);

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
	 * @param ontologyTerm
	 * @return a list of untyped child ontologyTerm entities from current
	 *         ontologyTerm in specified ontology
	 * @throws NullPointerException
	 *             if the ontologyIri is not known
	 * @throws NullPointerException
	 *             if the ontologyTermIri is not known
	 */
	Iterable<Entity> getChildOntologyTermEntities(String ontologyIri, String ontologyTermIri);

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

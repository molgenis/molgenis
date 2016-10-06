package org.molgenis.ontology.core.service;

import org.molgenis.ontology.core.meta.OntologyEntity;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.SemanticType;

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
	 * @param ontologyIds IDs of ontologies to search in
	 * @param terms       search terms
	 * @param pageSize    number of results to return.
	 * @return List of {@link OntologyTerm}s that match the search term.
	 */
	List<OntologyTerm> findExactOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

	/**
	 * Finds ontology terms that match a certain search string.
	 *
	 * @param ontologyIds IDs of ontologies to search in
	 * @param terms       search terms
	 * @param pageSize    number of results to return.
	 * @return List of {@link OntologyTerm}s that match the search term.
	 */
	List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

	/**
	 * Finds all {@link OntologyTerm}s in the give ontologyTerm scope
	 *
	 * @param ontologyIds IDs of ontologies to search in
	 * @param terms       search terms
	 * @param pageSize    number of results to return
	 * @param ontologyTermDomains scope of {@link OntologyTerm}s to search in
	 * @return List of {@link OntologyTerm}s
	 */
	List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize,
			List<OntologyTerm> ontologyTermDomains);

	/**
	 * Retrieve all ontology terms from the specified ontology
	 *
	 * @param ontologyId ID of the ontology to get all terms for
	 * @return all the {@link OntologyTerm}
	 */
	List<OntologyTerm> getAllOntologyTerms(String ontologyId);

	// voor de tag service

	/**
	 * Retrieves an OntologyTerm based on its IRI.
	 *
	 * @param iri the IRI of the ontology term to retrieve
	 * @return {@link OntologyTerm} with the specified IRI
	 */
	OntologyTerm getOntologyTerm(String iri);

	/**
	 * Retrieve a list of {@link OntologyTerm}s based on the given iris
	 *
	 * @param iris {@link List} of IRIs to look for
	 * @return List of {@link OntologyTerm}s found
	 */
	List<OntologyTerm> getOntologyTerms(List<String> iris);

	/**
	 * Retrieves all parents of an ontology term
	 *
	 * @param ontologyTerm the ontologyterm for which the parents are retrieved
	 * @return Iterable with the parent {@link OntologyTerm}s.
	 */
	Iterable<OntologyTerm> getAllParents(OntologyTerm ontologyTerm);

	/**
	 * Retrieves {@link OntologyTerm} parents with a max level indicating at which level in the hierarchy the
	 * children should be retrieved.
	 *
	 * @param ontologyTerm
	 * @param continuePredicate
	 * @return
	 */
	Iterable<OntologyTerm> getParents(OntologyTerm ontologyTerm, int maxLevel);

	/**
	 * Get all {@link OntologyTerm} children
	 *
	 * @param ontologyTerm
	 * @return
	 */
	Iterable<OntologyTerm> getAllChildren(OntologyTerm ontologyTerm);

	/**
	 * Retrieves {@link OntologyTerm} children with a max level indicating at which level in the hierarchy the
	 * children should be retrieved.
	 *
	 * @param ontologyTerm
	 * @param maxLevel
	 * @return
	 */
	Iterable<OntologyTerm> getChildren(OntologyTerm ontologyTerm, int maxLevel);

	/**
	 * Calculate distance between two {@link OntologyTerm}s
	 *
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	Integer getOntologyTermDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2);

	/**
	 * Calculate relatedness between two {@link OntologyTerm}s by the 2 * overlap / (depth1 + depth2)
	 *
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	Double getOntologyTermSemanticRelatedness(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2);

	/**
	 * Check if the first {@link OntologyTerm} is either the parent of or child of the second
	 * {@link OntologyTerm}
	 *
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @param stopLevel
	 * @return
	 */
	boolean related(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2, int stopLevel);

	/**
	 * is the first {@link OntologyTerm} descendant of the second {@link OntologyTerm}
	 *
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	boolean isDescendant(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2);

	/**
	 * Check if the distance between two {@link OntologyTerm}s is within the maxDistance
	 *
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @param maxDistance
	 * @return
	 */
	boolean areWithinDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2, int maxDistance);

	/**
	 * Retrieves all ontologies ids.
	 *
	 * @return String Ontology Id
	 */
	List<String> getAllOntologiesIds();

	/**
	 * Get all {@link SemanticType}s
	 *
	 * @return a list of {@link SemanticType}s
	 */
	List<SemanticType> getAllSemanticTypes();
}

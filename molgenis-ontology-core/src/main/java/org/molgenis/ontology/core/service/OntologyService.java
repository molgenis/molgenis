package org.molgenis.ontology.core.service;

import java.util.List;
import java.util.Set;

import org.molgenis.ontology.core.meta.OntologyEntity;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.model.SemanticType;

public interface OntologyService
{
	/**
	 * Retrieves all ontologies.
	 */
	List<Ontology> getOntologies();

	/**
	 * Retrieves a specific ontology
	 * 
	 * @param iri
	 *            IRI of the ontology to retrieve.
	 * @return the Ontology
	 */
	Ontology getOntology(String iri);

	/**
	 * Finds ontology terms that are exact matches to a certain search string.
	 * 
	 * @param ontologies
	 *            {@link OntologyEntity}s to search in
	 * @param search
	 *            search term
	 * @param pageSize
	 *            number of results to return.
	 * @return List of {@link OntologyTermImpl}s that match the search term.
	 */
	List<OntologyTermImpl> findExcatOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

	/**
	 * Finds ontology terms that match a certain search string.
	 * 
	 * @param ontologies
	 *            {@link OntologyEntity}s to search in
	 * @param search
	 *            search term
	 * @param pageSize
	 *            number of results to return.
	 * @return List of {@link OntologyTermImpl}s that match the search term.
	 */
	List<OntologyTermImpl> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

	/**
	 * Finds all {@link OntologyTermImpl}s in the give ontologyTerm scope
	 * 
	 * @param ontologyIds
	 * @param terms
	 * @param pageSize
	 * @param ontologyTermDomains
	 * @return
	 */
	List<OntologyTermImpl> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize,
			List<OntologyTermImpl> ontologyTermDomains);

	/**
	 * Retrieve all ontology terms from the specified ontology
	 * 
	 * @param ontologyIri
	 * 
	 * @return all the {@link OntologyTermImpl}
	 */
	List<OntologyTermImpl> getAllOntologyTerms(String ontologyIri);

	// voor de tag service
	/**
	 * Retrieves a specific OntologyTerm
	 * 
	 * @param ontology
	 *            the IRI of the {@link OntologyEntity} to search in
	 * @param IRI
	 *            comma separated list of IRIs to look for
	 * 
	 * @return Combined {@link OntologyTermImpl} for all IRI's listed
	 */
	OntologyTermImpl getOntologyTerm(String iri);

	/**
	 * Retrieve a list of {@link OntologyTermImpl}s based on the given iris
	 * 
	 * @param iris
	 * @return
	 */
	List<OntologyTermImpl> getOntologyTerms(List<String> iris);

	/**
	 * Retrieves all parents
	 * 
	 * @param ontologyTermImpl
	 * @return
	 */
	Iterable<OntologyTermImpl> getAllParents(OntologyTermImpl ontologyTermImpl);

	/**
	 * Retrieves {@link OntologyTermImpl} parents with a max level indicating at which level in the hierarchy the
	 * children should be retrieved.
	 * 
	 * @param ontologyTermImpl
	 * @param continuePredicate
	 * @return
	 */
	Iterable<OntologyTermImpl> getParents(OntologyTermImpl ontologyTermImpl, int maxLevel);

	/**
	 * Get all {@link OntologyTermImpl} children
	 * 
	 * @param ontologyTermImpl
	 * @return
	 */
	Iterable<OntologyTermImpl> getAllChildren(OntologyTermImpl ontologyTermImpl);

	/**
	 * Retrieves {@link OntologyTermImpl} children with a max level indicating at which level in the hierarchy the
	 * children should be retrieved.
	 * 
	 * @param ontologyTermImpl
	 * @param maxLevel
	 * @return
	 */
	Iterable<OntologyTermImpl> getChildren(OntologyTermImpl ontologyTermImpl, int maxLevel);

	/**
	 * Calculate distance between two {@link OntologyTermImpl}s
	 * 
	 * @param ontologyTermImpl1
	 * @param ontologyTermImpl2
	 * @return
	 */
	Integer getOntologyTermDistance(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2);

	/**
	 * Calculate relatedness between two {@link OntologyTermImpl}s by the 2 * overlap / (depth1 + depth2)
	 * 
	 * @param ontologyTermImpl1
	 * @param ontologyTermImpl2
	 * @return
	 */
	Double getOntologyTermSemanticRelatedness(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2);

	/**
	 * Check if the first {@link OntologyTermImpl} is either the parent of or child of the second
	 * {@link OntologyTermImpl}
	 * 
	 * @param ontologyTermImpl1
	 * @param ontologyTermImpl2
	 * @param stopLevel
	 * @return
	 */
	boolean related(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2, int stopLevel);

	/**
	 * is the first {@link OntologyTermImpl} descendant of the second {@link OntologyTermImpl}
	 * 
	 * @param ontologyTermImpl1
	 * @param ontologyTermImpl2
	 * @return
	 */
	boolean isDescendant(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2);

	/**
	 * Check if the distance between two {@link OntologyTermImpl}s is within the maxDistance
	 * 
	 * @param ontologyTermImpl1
	 * @param ontologyTermImpl2
	 * @param maxDistance
	 * @return
	 */
	boolean areWithinDistance(OntologyTermImpl ontologyTermImpl1, OntologyTermImpl ontologyTermImpl2, int maxDistance);

	/**
	 * Retrieves all ontologies ids.
	 * 
	 * @return String Ontology Id
	 */
	List<String> getAllOntologiesIds();

	/**
	 * Get all {@link SemanticTypey}s
	 * 
	 * @return a list of {@link SemanticType}s
	 */
	List<SemanticType> getAllSemanticTypes();
}

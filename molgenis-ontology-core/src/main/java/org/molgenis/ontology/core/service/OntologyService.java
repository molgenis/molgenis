package org.molgenis.ontology.core.service;

import java.util.List;
import java.util.Set;

import org.molgenis.ontology.core.meta.OntologyEntity;
import org.molgenis.ontology.core.meta.SemanticTypeEntity;
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
	 * @param scope
	 * @return
	 */
	List<OntologyTermImpl> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize,
			List<OntologyTermImpl> scope);

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
	 * @param ontologyTerm
	 * @return
	 */
	Iterable<OntologyTermImpl> getAllParents(OntologyTermImpl ontologyTerm);

	/**
	 * Retrieves {@link OntologyTermImpl} parents with a max level indicating at which level in the hierarchy the
	 * children should be retrieved.
	 * 
	 * @param ontologyTerm
	 * @param continuePredicate
	 * @return
	 */
	Iterable<OntologyTermImpl> getParents(OntologyTermImpl ontologyTerm, int maxLevel);

	/**
	 * Get all {@link OntologyTermImpl} children
	 * 
	 * @param ontologyTerm
	 * @return
	 */
	Iterable<OntologyTermImpl> getAllChildren(OntologyTermImpl ontologyTerm);

	/**
	 * Retrieves {@link OntologyTermImpl} children with a max level indicating at which level in the hierarchy the
	 * children should be retrieved.
	 * 
	 * @param ontologyTerm
	 * @param continuePredicate
	 * @return
	 */
	Iterable<OntologyTermImpl> getChildren(OntologyTermImpl ontologyTerm, int maxLevel);

	/**
	 * Calculate distance between two ontology terms
	 * 
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	Integer getOntologyTermDistance(OntologyTermImpl ontologyTerm1, OntologyTermImpl ontologyTerm2);

	/**
	 * Calculate relatedness between two ontology terms by the 2 * overlap / (depth1 + depth2)
	 * 
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	Double getOntologyTermSemanticRelatedness(OntologyTermImpl ontologyTerm1, OntologyTermImpl ontologyTerm2);

	/**
	 * Check if the first {@link OntologyTermImpl} is either the parent of or child of the second
	 * {@link OntologyTermImpl}
	 * 
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @param stopLevel
	 * @return
	 */
	boolean related(OntologyTermImpl ontologyTerm1, OntologyTermImpl ontologyTerm2, int stopLevel);

	/**
	 * is the first {@link OntologyTermImpl} descendant of the second {@link OntologyTermImpl}
	 * 
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	boolean isDescendant(OntologyTermImpl ontologyTerm1, OntologyTermImpl ontologyTerm2);

	boolean areWithinDistance(OntologyTermImpl ontologyTerm1, OntologyTermImpl ontologyTerm2, int maxDistance);

	/**
	 * Retrieves all ontologies ids.
	 * 
	 * @return String Ontology Id
	 */
	List<String> getAllOntologiesIds();

	/**
	 * Get all {@link SemanticTypeEntity}s
	 * 
	 * @return a list of {@link SemanticTypeEntity}s
	 */
	List<SemanticType> getAllSemanticTypes();
}

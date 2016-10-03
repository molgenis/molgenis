package org.molgenis.ontology.core.service;

import java.util.List;
import java.util.Set;

import org.molgenis.ontology.core.meta.OntologyEntity;
import org.molgenis.ontology.core.meta.SemanticTypeEntity;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
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
	 * @return List of {@link OntologyTerm}s that match the search term.
	 */
	List<OntologyTerm> findExcatOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

	/**
	 * Finds ontology terms that match a certain search string.
	 * 
	 * @param ontologies
	 *            {@link OntologyEntity}s to search in
	 * @param search
	 *            search term
	 * @param pageSize
	 *            number of results to return.
	 * @return List of {@link OntologyTerm}s that match the search term.
	 */
	List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

	/**
	 * Finds all {@link OntologyTerm}s in the give ontologyTerm scope
	 * 
	 * @param ontologyIds
	 * @param terms
	 * @param pageSize
	 * @param scope
	 * @return
	 */
	List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize,
			List<OntologyTerm> scope);

	/**
	 * Retrieve all ontology terms from the specified ontology
	 * 
	 * @param ontologyIri
	 * 
	 * @return all the {@link OntologyTerm}
	 */
	List<OntologyTerm> getAllOntologyTerms(String ontologyIri);

	// voor de tag service
	/**
	 * Retrieves a specific OntologyTerm
	 * 
	 * @param ontology
	 *            the IRI of the {@link OntologyEntity} to search in
	 * @param IRI
	 *            comma separated list of IRIs to look for
	 * 
	 * @return Combined {@link OntologyTerm} for all IRI's listed
	 */
	OntologyTerm getOntologyTerm(String iri);

	/**
	 * Retrieves a list of atomic {@link OntologyTerm}s if the provided Array of {@link OntologyTerm}s has any composite
	 * iris such as "iri1,iri2"
	 * 
	 * @param Array
	 *            of {@link OntologyTerm}
	 * @return a list of atomic {@link OntologyTerm}
	 */
	List<OntologyTerm> getAtomicOntologyTerms(OntologyTerm ontologyTerm);

	/**
	 * Retrieves all parents
	 * 
	 * @param ontologyTerm
	 * @return
	 */
	Iterable<OntologyTerm> getAllParents(OntologyTerm ontologyTerm);

	/**
	 * Retrieves {@link OntologyTerm} parents with a max level indicating at which level in the hierarchy the children
	 * should be retrieved.
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
	 * Retrieves {@link OntologyTerm} children with a max level indicating at which level in the hierarchy the children
	 * should be retrieved.
	 * 
	 * @param ontologyTerm
	 * @param continuePredicate
	 * @return
	 */
	Iterable<OntologyTerm> getChildren(OntologyTerm ontologyTerm, int maxLevel);

	/**
	 * Calculate distance between two ontology terms
	 * 
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	Integer getOntologyTermDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2);

	/**
	 * Calculate relatedness between two ontology terms by the 2 * overlap / (depth1 + depth2)
	 * 
	 * @param ontologyTerm1
	 * @param ontologyTerm2
	 * @return
	 */
	Double getOntologyTermSemanticRelatedness(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2);

	/**
	 * Check if the first {@link OntologyTerm} is either the parent of or child of the second {@link OntologyTerm}
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

	boolean areWithinDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2, int maxDistance);

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

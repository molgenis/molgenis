package org.molgenis.data.semanticsearch.service;

import org.molgenis.data.semanticsearch.explain.bean.OntologyTermHit;
import org.molgenis.data.semanticsearch.explain.criteria.MatchingCriterion;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.model.SemanticType;

import java.util.List;
import java.util.Set;

public interface TagGroupGenerator
{
	/**
	 * Generate a list of {@link TagGroup}s for one query {@link String}
	 *
	 * @param queryString
	 * @param ontologyIds
	 * @return a list of {@link TagGroup}s
	 */
	List<TagGroup> generateTagGroups(String queryString, List<String> ontologyIds);

	/**
	 * Generate a list of {@link TagGroup}s with {@link OntologyTerm}s whose {@link SemanticType}s are key concepts
	 *
	 * @param queryString
	 * @param ontologyIds
	 * @param keyConcepts
	 * @return a list of {@link OntologyTermHit}s
	 */
	List<TagGroup> generateTagGroups(String queryString, List<String> ontologyIds, List<SemanticType> keyConcepts);

	/**
	 * Combine the qualified {@link OntologyTermHit}s to obtain the composite {@link TagGroup}s (consisting of multiple
	 * ontology terms)
	 *
	 * @param queryWords
	 * @param ontologyTermHits
	 * @return a list of combined {@link TagGroup}s
	 */
	List<TagGroup> combineTagGroups(Set<String> queryWords, List<OntologyTermHit> ontologyTermHits);

	/**
	 * Filter the relevant {@link OntologyTerm}s by applying the {@link MatchingCriterion} and generate a list of
	 * {@link OntologyTermHit}s based on the qualified {@link OntologyTerm}s
	 *
	 * @param relevantOntologyTerms
	 * @param searchTerms
	 * @param matchingCriterion
	 * @return
	 */
	List<OntologyTermHit> applyTagMatchingCriterion(List<OntologyTerm> relevantOntologyTerms, Set<String> searchTerms,
			MatchingCriterion matchingCriterion);

	;
}

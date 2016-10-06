package org.molgenis.data.semanticsearch.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.ontology.core.model.OntologyTerm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SemanticSearchService
{
	/**
	 * Find all relevant source attributes with an explanation based on ontology terms and search terms
	 *
	 * @param sourceEntityType
	 * @param queryTerms
	 * @param ontologyTerms
	 * @return Attribute of resembling attributes, sorted by relevance
	 */
	Map<Attribute, ExplainedAttribute> findAttributes(EntityType sourceEntityType, Set<String> queryTerms,
			Collection<OntologyTerm> ontologyTerms);

	/**
	 * A decision tree for getting the relevant attributes
	 * <p>
	 * 1. First find attributes based on searchTerms. 2. Second find attributes based on ontology terms from tags 3.
	 * Third find attributes based on target attribute label.
	 *
	 * @return Attribute of resembling attributes, sorted by relevance
	 */
	Map<Attribute, ExplainedAttribute> decisionTreeToFindRelevantAttributes(EntityType sourceEntityType,
			Attribute targetAttribute, Collection<OntologyTerm> ontologyTermsFromTags, Set<String> searchTerms);

	/**
	 * Finds {@link OntologyTerm}s that can be used to tag an attribute.
	 *
	 * @param entity      name of the entity
	 * @param ontologyIDs IDs of ontologies to take the {@link OntologyTerm}s from.
	 * @return {@link Map} of {@link Hit}s for {@link OntologyTerm} results
	 */
	Map<Attribute, Hit<OntologyTerm>> findTags(String entity, List<String> ontologyIDs);

	/**
	 * Finds {@link OntologyTerm}s for an attribute.
	 *
	 * @param attribute   Attribute to tag
	 * @param ontologyIds IDs of ontologies to take the {@link OntologyTerm}s from.
	 * @return {@link List} of {@link Hit}s for {@link OntologyTerm}s found, most relevant first
	 */
	Hit<OntologyTerm> findTags(Attribute attribute, List<String> ontologyIds);
}

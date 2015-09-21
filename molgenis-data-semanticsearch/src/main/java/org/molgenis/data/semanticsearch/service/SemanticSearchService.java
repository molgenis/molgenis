package org.molgenis.data.semanticsearch.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.ontology.core.model.OntologyTerm;

public interface SemanticSearchService
{
	/**
	 * Find all relevant source attributes with an explanation based on ontology terms and search terms
	 * 
	 * @param source
	 * @param attributeMetaData
	 * @param ontologyTerms
	 * 
	 * @return AttributeMetaData of resembling attributes, sorted by relevance
	 */
	Map<AttributeMetaData, ExplainedAttributeMetaData> findAttributes(EntityMetaData sourceEntityMetaData,
			Set<String> queryTerms, Collection<OntologyTerm> ontologyTerms);

	/**
	 * A decision tree for getting the relevant attributes
	 * 
	 * 1. First find attributes based on searchTerms. 2. Second find attributes based on ontology terms from tags 3.
	 * Third find attributes based on target attribute label.
	 * 
	 * @return AttributeMetaData of resembling attributes, sorted by relevance
	 */
	Map<AttributeMetaData, ExplainedAttributeMetaData> decisionTreeToFindRelevantAttributes(
			EntityMetaData sourceEntityMetaData, AttributeMetaData targetAttribute,
			Collection<OntologyTerm> ontologyTermsFromTags, Set<String> searchTerms);

	/**
	 * Finds {@link OntologyTerm}s that can be used to tag an attribute.
	 * 
	 * @param entity
	 *            name of the entity
	 * @param ontologies
	 *            IDs of ontologies to take the {@link OntologyTerm}s from.
	 * @return {@link Map} of {@link Hit}s for {@link OntologyTerm} results
	 */
	Map<AttributeMetaData, Hit<OntologyTerm>> findTags(String entity, List<String> ontologyIDs);

	/**
	 * Finds {@link OntologyTerm}s for an attribute.
	 * 
	 * @param attribute
	 *            AttributeMetaData to tag
	 * @param ontologyIds
	 *            IDs of ontologies to take the {@link OntologyTerm}s from.
	 * @return {@link List} of {@link Hit}s for {@link OntologyTerm}s found, most relevant first
	 */
	Hit<OntologyTerm> findTags(AttributeMetaData attribute, List<String> ontologyIds);
}

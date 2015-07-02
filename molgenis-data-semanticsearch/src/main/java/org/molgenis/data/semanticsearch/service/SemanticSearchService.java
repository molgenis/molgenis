package org.molgenis.data.semanticsearch.service;

import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.ontology.core.model.OntologyTerm;

public interface SemanticSearchService
{
	/**
	 * Find all relevant source attributes for the specified target attribute
	 * 
	 * @param source
	 * @param target
	 * @param attributeMetaData
	 * @return AttributeMetaData of resembling attributes, sorted by relevance
	 */
	Iterable<AttributeMetaData> findAttributes(org.molgenis.data.EntityMetaData source, EntityMetaData target,
			AttributeMetaData attributeMetaData);

	/**
	 * Find all relevant source attributes with an explanation
	 * 
	 * @param source
	 * @param target
	 * @param attributeMetaData
	 * @return AttributeMetaData of resembling attributes, sorted by relevance
	 */
	Map<AttributeMetaData, Iterable<ExplainedQueryString>> explainAttributes(EntityMetaData source,
			EntityMetaData target, AttributeMetaData attributeMetaData);

	/**
	 * Finds {@link OntologyTerm}s that can be used to tag an attribute. >>>>>>> 3b45cb9... added a method in
	 * MappingServiceController that allows us to get the explanations per suggested attributes in json format
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

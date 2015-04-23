package org.molgenis.data.semanticsearch.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
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
	 * Finds {@link OntologyTerm}s that can be used to tag an attribute.
	 * 
	 * @param entity
	 *            name of the entity
	 * @param ontologies
	 *            IDs of ontologies to take the {@link OntologyTerm}s from.
	 * @return {@link Map} of {@link OntologyTerm} results
	 */
	Map<AttributeMetaData, List<OntologyTerm>> findTags(String entity, List<String> ontologyIDs);

	/**
	 * Finds {@link OntologyTerm}s for an attribute.
	 * 
	 * @param attribute
	 *            AttributeMetaData to tag
	 * @param ontologyIds
	 *            IDs of ontologies to take the {@link OntologyTerm}s from.
	 * @return {@link Future} for the {@link List} of {@link OntologyTerm}s found {@link semanticSearchService.findTags}
	 */
	List<OntologyTerm> findTags(AttributeMetaData attribute, List<String> ontologyIds);

}

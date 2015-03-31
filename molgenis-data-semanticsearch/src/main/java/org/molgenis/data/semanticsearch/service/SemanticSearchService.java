package org.molgenis.data.semanticsearch.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.semanticsearch.semantic.ItemizedSearchResult;
import org.molgenis.ontology.repository.model.OntologyTerm;

public interface SemanticSearchService
{
	/**
	 * Finds more attributes from all kinds of packages, that semantically resemble the attribute that is provided.
	 * 
	 * @param p
	 *            the Package that will be searched for attributes
	 * @param attributeMetaData
	 *            attribute that the results should resemble
	 * @return AttributeMetaData of resembling attributes, sorted by relevance, not including @attributeMetaData
	 */
	Iterable<AttributeMetaData> findAttributes(org.molgenis.data.Package p, AttributeMetaData attributeMetaData);

	/**
	 * Searches the packages and their entities and attributes, and tags thereon for a search term.
	 * 
	 * @param searchTerm
	 *            the term to search for
	 * @return {@link Iterable} of {@link ItemizedSearchResult}s containing {@link Package}s, sorted by descending
	 *         relevance
	 */
	Iterable<ItemizedSearchResult<Package>> findPackages(String searchTerm);

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
	 * @return {@link Future} for the {@link List} of {@link OntologyTerm}s found
	 */
	List<OntologyTerm> findTags(AttributeMetaData attribute, List<String> ontologyIds);

}

package org.molgenis.data.semantic;

import org.molgenis.data.AttributeMetaData;

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

}

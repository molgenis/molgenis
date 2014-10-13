package org.molgenis.data.semantic;

import org.molgenis.data.AttributeMetaData;

public interface SemanticSearchService
{
	/**
	 * Finds more attributes from all kinds of packages, that semantically
	 * resemble the attribute that is provided.
	 * 
	 * @param p
	 *            the Package that will be searched for attributes
	 * @param attributeMetaData
	 *            attribute that the results should resemble
	 * @return AttributeMetaData of resembling attributes, sorted by relevance,
	 *         not including @attributeMetaData
	 */
	Iterable<AttributeMetaData> findAttributes(Package p, AttributeMetaData attributeMetaData);

	// for BMB, a bit later
	// Iterable<Object> googleSearchFindStuff(String searchTerm);
}

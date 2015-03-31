package org.molgenis.data.semanticsearch.semantic;

public interface SearchResult<ItemType>
{
	ItemType getItem();

	int getRelevance();

}

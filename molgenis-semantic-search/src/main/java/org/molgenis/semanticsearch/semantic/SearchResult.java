package org.molgenis.semanticsearch.semantic;

public interface SearchResult<ItemType>
{
	ItemType getItem();

	int getRelevance();

}

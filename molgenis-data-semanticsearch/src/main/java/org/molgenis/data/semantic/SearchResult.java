package org.molgenis.data.semantic;

public interface SearchResult<ItemType>
{
	ItemType getItem();

	int getRelevance();

}

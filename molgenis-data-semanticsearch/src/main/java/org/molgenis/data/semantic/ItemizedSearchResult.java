package org.molgenis.data.semantic;

public interface ItemizedSearchResult<ItemType> extends SearchResult<ItemType>
{
	Iterable<SearchResult<?>> getRelevantSubItems();
}

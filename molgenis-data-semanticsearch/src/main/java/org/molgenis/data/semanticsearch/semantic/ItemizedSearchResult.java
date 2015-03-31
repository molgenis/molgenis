package org.molgenis.data.semanticsearch.semantic;

public interface ItemizedSearchResult<ItemType> extends SearchResult<ItemType>
{
	Iterable<SearchResult<?>> getRelevantSubItems();
}

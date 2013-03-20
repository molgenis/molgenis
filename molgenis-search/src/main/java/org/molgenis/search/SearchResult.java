package org.molgenis.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Result of a search. Check the errorMessage, if not null an error was returned
 * by the SearchService
 * 
 * @author erwin
 * 
 */
public class SearchResult implements Iterable<Hit>
{
	private long totalHitCount = 0;
	private List<Hit> searchHits = Collections.emptyList();
	private String errorMessage = null;

	public SearchResult(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public SearchResult(long totalHitCount, List<Hit> searchHits)
	{
		if (searchHits == null) throw new IllegalArgumentException("SearchHits is null");

		this.totalHitCount = totalHitCount;
		this.searchHits = searchHits;
	}

	public long getTotalHitCount()
	{
		return totalHitCount;
	}

	public List<Hit> getSearchHits()
	{
		return Collections.unmodifiableList(searchHits);
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public Iterator<Hit> iterator()
	{
		return searchHits.iterator();
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

}

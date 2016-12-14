package org.molgenis.data.elasticsearch.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.molgenis.data.aggregation.AggregateResult;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Result of a search. Check the errorMessage, if not null an error was returned by the SearchService
 *
 * @author erwin
 */
public class SearchResult implements Iterable<Hit>
{
	private long totalHitCount = 0;
	private List<Hit> searchHits = Collections.emptyList();
	private String errorMessage = null;
	private AggregateResult aggregate;

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

	public SearchResult(long totalHitCount, List<Hit> searchHits, AggregateResult aggregate)
	{
		this(totalHitCount, searchHits);
		this.aggregate = aggregate;
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

	public AggregateResult getAggregate()
	{
		return aggregate;
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
package org.molgenis.data.omx;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;

/**
 * Executes a search query and adapts the resulting hit iterator to an entity iterator
 * 
 * If no pagesize is set the results are retrieved in 1000 hits batches until the total count is reached
 */
public class OmxRepositoryIterator implements Iterator<Entity>
{
	private static final int BATCH_SIZE = 1000;

	private final String dataSetIdentifier;
	private final SearchService searchService;
	private Iterator<Hit> hits;
	private final Set<String> attributeNames;
	private final long pageSize;
	private int count;
	private final Query query;

	public OmxRepositoryIterator(String dataSetIdentifier, SearchService searchService, Query q,
			Set<String> attributeNames)
	{
		this.dataSetIdentifier = dataSetIdentifier;
		this.searchService = searchService;
		this.attributeNames = attributeNames;

		query = q.getPageSize() == 0 ? new QueryImpl(q).pageSize(BATCH_SIZE) : q;
		SearchRequest request = new SearchRequest(dataSetIdentifier, query, null);

		SearchResult result = searchService.search(request);
		pageSize = q.getPageSize() == 0 ? result.getTotalHitCount() : q.getPageSize();

		hits = result.iterator();
	}

	@Override
	public boolean hasNext()
	{
		return count < pageSize;
	}

	@Override
	public Entity next()
	{
		if (!hits.hasNext())
		{
			query.offset(count);
			SearchRequest request = new SearchRequest(dataSetIdentifier, query, null);
			SearchResult result = searchService.search(request);
			hits = result.iterator();
		}

		Hit hit = hits.next();
		count++;

		return hitToEntity(hit);
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("Remove not supported");
	}

	private Entity hitToEntity(Hit hit)
	{
		MapEntity entity = new MapEntity("id");
		Map<String, Object> values = hit.getColumnValueMap();

		for (Map.Entry<String, Object> entry : values.entrySet())
		{
			String attr = entry.getKey();
			if (attributeNames.contains(attr))
			{
				entity.set(attr.toLowerCase(), entry.getValue());
			}
		}

		return entity;
	}
}

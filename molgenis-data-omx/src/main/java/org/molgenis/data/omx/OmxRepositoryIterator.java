package org.molgenis.data.omx;

import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.util.Hit;
import org.molgenis.data.elasticsearch.util.SearchRequest;
import org.molgenis.data.elasticsearch.util.SearchResult;
import org.molgenis.data.support.QueryImpl;

/**
 * Executes a search query and adapts the resulting hit iterator to an entity iterator
 * 
 * If no pagesize is set the results are retrieved in 1000 hits batches until the total count is reached
 */
public class OmxRepositoryIterator implements Iterator<Entity>
{
	static final int BATCH_SIZE = 1000;

	private final String dataSetIdentifier;
	private final SearchService searchService;
	private final DataService dataService;
	private final Set<String> attributeNames;
	private long pageSize;
	private final Query query;

	private int count;
	private Iterator<Hit> hits;

	private transient EntityMetaData cachedEntityMetaData;

	public OmxRepositoryIterator(String dataSetIdentifier, SearchService searchService, DataService dataService,
			Query q, Set<String> attributeNames)
	{
		this.dataSetIdentifier = dataSetIdentifier;
		this.searchService = searchService;
		this.dataService = dataService;
		this.attributeNames = attributeNames;

		query = q.getPageSize() == 0 ? new QueryImpl(q).pageSize(BATCH_SIZE) : q;
		SearchRequest request = new SearchRequest(dataSetIdentifier, query, null);

		SearchResult result = searchService.search(request);
		long maxHitCount = result.getTotalHitCount() - q.getOffset();
		if (q.getPageSize() == 0)
		{
			pageSize = maxHitCount;
		}
		else
		{
			pageSize = maxHitCount < q.getPageSize() ? maxHitCount : q.getPageSize();
		}

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

		if (cachedEntityMetaData == null)
		{
			cachedEntityMetaData = dataService.getEntityMetaData(dataSetIdentifier);
		}
		return new HitEntity(hit, attributeNames, cachedEntityMetaData, dataService);
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("Remove not supported");
	}

}

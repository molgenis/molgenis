package org.molgenis.data.support;

import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.util.BatchingIterable;

/**
 * BatchingIterable that batches a Query.
 * 
 * It changes the query's offset and pageSize of each batch.
 */
public abstract class BatchingQueryResult extends BatchingIterable<Entity>
{
	private final Query query;

	public BatchingQueryResult(int batchSize, Query query)
	{
		super(batchSize, query.getOffset(), query.getPageSize());
		this.query = query;
	}

	@Override
	protected List<Entity> getBatch(int offset, int batchSize)
	{
		Query batchQuery;
		if (offset != query.getOffset() || batchSize != query.getPageSize())
		{
			batchQuery = new QueryImpl(query).setOffset(offset).setPageSize(batchSize);
		}
		else
		{
			batchQuery = query;
		}
		return getBatch(batchQuery);
	}

	protected abstract List<Entity> getBatch(Query q);

}

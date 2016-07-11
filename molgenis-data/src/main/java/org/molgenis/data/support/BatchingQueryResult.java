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
public abstract class BatchingQueryResult<E extends Entity> extends BatchingIterable<E>
{
	private final Query<E> query;

	public BatchingQueryResult(int batchSize, Query<E> query)
	{
		super(batchSize, query.getOffset(), query.getPageSize());
		this.query = query;
	}

	@Override
	protected List<E> getBatch(int offset, int batchSize)
	{
		Query<E> batchQuery;
		if (offset != query.getOffset() || batchSize != query.getPageSize())
		{
			batchQuery = new QueryImpl<>(query).setOffset(offset).setPageSize(batchSize);
		}
		else
		{
			batchQuery = query;
		}
		return getBatch(batchQuery);
	}

	protected abstract List<E> getBatch(Query<E> q);

}

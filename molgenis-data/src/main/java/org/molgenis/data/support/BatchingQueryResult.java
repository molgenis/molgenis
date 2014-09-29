package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.util.BatchingIterable;

/**
 * BatchingIterable that batches a Query.
 * 
 * It changes the query's offset and pageSize of each batch.
 * 
 * Do NOT use this when there is already an offset or pageSize set on the query because it will be overwritten.
 * 
 */
public abstract class BatchingQueryResult extends BatchingIterable<Entity>
{
	private final Query query;

	public BatchingQueryResult(int batchSize, Query query)
	{
		super(batchSize);
		this.query = query;
	}

	@Override
	protected Iterable<Entity> getBatch(int offset, int batchSize)
	{
		Query q = new QueryImpl(query).setOffset(offset).setPageSize(batchSize);
		return getBatch(q);
	}

	protected abstract Iterable<Entity> getBatch(Query q);

}

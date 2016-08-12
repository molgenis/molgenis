package org.molgenis.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterable that returns an iterator that retrieves a new batch of objects after a given batchSize
 *
 * @param <T>
 */
public abstract class BatchingIterable<T> implements Iterable<T>
{
	private final int batchSize;
	private final int offset;
	/**
	 * Limit > 0: Number of elements to retrieve, Limit = 0: Limit undefined
	 */
	private final int limit;

	public BatchingIterable(int batchSize)
	{
		this(batchSize, 0, 0);
	}

	public BatchingIterable(int batchSize, int offset, int limit)
	{
		if (batchSize <= 0) throw new IllegalArgumentException("BatchSize must be greated then 0");
		if (offset < 0) throw new IllegalArgumentException("Offset must be larger than or equal to 0");
		if (limit < 0) throw new IllegalArgumentException("Limit must be larger than or equal to 0");
		this.batchSize = batchSize;
		this.offset = offset;
		this.limit = limit;
	}

	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			/**
			 * Element index
			 */
			private int index = offset;
			/**
			 * Element iterator for the current batch
			 */
			private Iterator<T> it;

			@Override
			public boolean hasNext()
			{
				boolean hasNext;

				// lazy load first batch
				if (it == null)
				{
					it = nextBatch();
					hasNext = it.hasNext();
					if (!hasNext)
					{
						return false;
					}
				}
				else
				{
					hasNext = it.hasNext();
				}

				if (!hasNext)
				{
					it = nextBatch();
					hasNext = it.hasNext();
				}

				return hasNext;
			}

			@Override
			public T next()
			{
				if (!hasNext())
				{
					throw new NoSuchElementException();
				}

				T element = it.next();
				++index;
				return element;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			private Iterator<T> nextBatch()
			{
				// calculate batch size
				int nextBatchSize;

				// always retrieve first batch: index == offset
				// retrieve next batch if previous batch contained less items then batch size
				if (index == offset || (index - offset) % batchSize == 0)
				{
					if (limit == 0)
					{
						nextBatchSize = batchSize;
					}
					else
					{
						if (index == offset + limit)
						{
							nextBatchSize = 0;
						}
						else if (index + batchSize <= offset + limit)
						{
							nextBatchSize = batchSize;
						}
						else
						{
							nextBatchSize = offset + limit - index;
						}
					}
				}
				else
				{
					nextBatchSize = 0;
				}

				if (nextBatchSize == 0)
				{
					return Collections.emptyIterator();
				}
				else
				{
					return getBatch(index, nextBatchSize).iterator();
				}
			}
		};
	}

	/**
	 * Return new batch, should not return null but empty list if no more elements are available
	 *
	 * @param offset    (startIndex)
	 * @param batchSize
	 * @return
	 */
	protected abstract Iterable<T> getBatch(int offset, int batchSize);
}

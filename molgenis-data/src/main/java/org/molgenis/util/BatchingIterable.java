package org.molgenis.util;

import java.util.Iterator;

/**
 * Iterable that returns an iterator that retrieves a new batch of objects after a given batchSize
 * 
 * @param <T>
 */
public abstract class BatchingIterable<T> implements Iterable<T>
{
	private final int batchSize;

	public BatchingIterable(int batchSize)
	{
		this.batchSize = batchSize;
		if (batchSize <= 0) throw new IllegalArgumentException("BatchSize must be greated then 0");
	}

	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			private int offset = 0;
			/**
			 * Index in this batch of last element returned by next(), -1 if next() was not called for this batch
			 */
			private int index = -1;
			private Iterator<T> it = getBatch(offset, batchSize).iterator();

			@Override
			public boolean hasNext()
			{
				boolean hasNext = it.hasNext();

				// retrieve new batch if current batch has no more items and the number of items in this batch equals
				// the batch size
				if (!hasNext && index == batchSize - 1)
				{
					offset += batchSize;
					it = getBatch(offset, batchSize).iterator();
					hasNext = it.hasNext();
					index = -1;
				}

				return hasNext;
			}

			@Override
			public T next()
			{
				T element = it.next();
				++index;
				return element;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Return new batch, should not return null but empty list if no more elements are available
	 * 
	 * @param offset
	 *            (startIndex)
	 * @param batchSize
	 * @return
	 */
	protected abstract Iterable<T> getBatch(int offset, int batchSize);
}

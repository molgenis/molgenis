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
			private Iterator<T> it = getBatch(offset, batchSize).iterator();

			@Override
			public boolean hasNext()
			{
				if (!it.hasNext())
				{
					offset += batchSize;
					it = getBatch(offset, batchSize).iterator();
				}

				return it.hasNext();
			}

			@Override
			public T next()
			{
				return it.next();
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

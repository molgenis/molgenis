package org.molgenis.util;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

public class BatchingIterableTest
{
	@Test
	public void iterator()
	{
		for (int i = 1; i < 12; i++)
		{
			iterator(i);
		}
	}

	private void iterator(final int batchSize)
	{
		final List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);

		Iterable<Integer> iterable = new BatchingIterable<Integer>(batchSize)
		{
			@Override
			protected List<Integer> getBatch(int startIndex, int batchSize)
			{
				int toIndex = startIndex + batchSize;
				if (startIndex >= items.size() - 1) return Collections.emptyList();
				toIndex = items.size() - 1 < toIndex ? items.size() - 1 : toIndex;
				return items.subList(startIndex, toIndex);
			}
		};

		int i = 1;
		Iterator<Integer> it = iterable.iterator();
		while (it.hasNext())
		{
			assertEquals(it.next().intValue(), i++);
		}
		assertEquals(i, 9);

	}
}

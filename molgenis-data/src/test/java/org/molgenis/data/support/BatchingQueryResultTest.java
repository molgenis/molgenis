package org.molgenis.data.support;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class BatchingQueryResultTest
{

	@Test
	public void getBatch()
	{
		final int batchSize = 2;
		BatchingQueryResult bqr = new BatchingQueryResult(batchSize, new QueryImpl())
		{
			int batchCount = 0;

			@Override
			protected List<Entity> getBatch(Query q)
			{
				assertEquals(q.getOffset(), batchCount * batchSize);
				assertEquals(q.getPageSize(), batchSize);

				if (++batchCount == 3) return Lists.newArrayList();
				return Arrays.<Entity> asList(new MapEntity(), new MapEntity());
			}
		};

		assertEquals(Iterables.size(bqr), 4);
	}
}

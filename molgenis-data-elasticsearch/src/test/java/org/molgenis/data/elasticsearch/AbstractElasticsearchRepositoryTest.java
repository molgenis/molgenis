package org.molgenis.data.elasticsearch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.testng.annotations.Test;

public class AbstractElasticsearchRepositoryTest
{
	@Test
	public void findOne() throws IOException
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		SearchService searchService = mock(SearchService.class);
		AbstractElasticsearchRepository repository = new AbstractElasticsearchRepository(searchService)
		{
			@Override
			public void rebuildIndex()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public EntityMetaData getEntityMetaData()
			{
				return entityMeta;
			}
		};
		try
		{
			Entity entity = mock(Entity.class);
			Object id = Integer.valueOf(0);
			Fetch fetch = new Fetch();
			when(searchService.get(id, entityMeta, fetch)).thenReturn(entity);
			assertEquals(repository.findOne(id, fetch), entity);
		}
		finally
		{
			repository.close();
		}
	}

	@Test
	public void findOneQuery() throws IOException
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		SearchService searchService = mock(SearchService.class);
		AbstractElasticsearchRepository repository = new AbstractElasticsearchRepository(searchService)
		{
			@Override
			public void rebuildIndex()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public EntityMetaData getEntityMetaData()
			{
				return entityMeta;
			}
		};
		try
		{
			Entity entity = mock(Entity.class);
			Query q = mock(Query.class);
			when(searchService.search(q, entityMeta)).thenReturn(Arrays.asList(entity));
			assertEquals(repository.findOne(q), entity);
		}
		finally
		{
			repository.close();
		}
	}

	@Test
	public void findOneQueryNoResults() throws IOException
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		SearchService searchService = mock(SearchService.class);
		AbstractElasticsearchRepository repository = new AbstractElasticsearchRepository(searchService)
		{
			@Override
			public void rebuildIndex()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public EntityMetaData getEntityMetaData()
			{
				return entityMeta;
			}
		};
		try
		{
			Query q = mock(Query.class);
			when(searchService.search(q, entityMeta)).thenReturn(Collections.emptyList());
			assertNull(repository.findOne(q));
		}
		finally
		{
			repository.close();
		}
	}

	@Test
	public void findAll() throws IOException
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		SearchService searchService = mock(SearchService.class);
		AbstractElasticsearchRepository repository = new AbstractElasticsearchRepository(searchService)
		{
			@Override
			public void rebuildIndex()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public EntityMetaData getEntityMetaData()
			{
				return entityMeta;
			}
		};
		try
		{
			Iterable<Entity> entities = Arrays.asList(mock(Entity.class));
			Iterable<Object> ids = Arrays.asList(0);
			Fetch fetch = new Fetch();
			when(searchService.get(ids, entityMeta, fetch)).thenReturn(entities);
			assertEquals(repository.findAll(ids, fetch), entities);
		}
		finally
		{
			repository.close();
		}
	}
}

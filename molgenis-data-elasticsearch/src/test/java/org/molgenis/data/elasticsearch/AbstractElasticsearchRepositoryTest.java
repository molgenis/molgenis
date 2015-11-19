package org.molgenis.data.elasticsearch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
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

package org.molgenis.data.elasticsearch;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.meta.model.EntityMetaData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ElasticsearchEntityFactoryTest
{
	private EntityManager entityManager;
	private EntityToSourceConverter entityToSourceConverter;
	private ElasticsearchEntityFactory elasticsearchEntityFactory;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityManager = mock(EntityManager.class);
		entityToSourceConverter = mock(EntityToSourceConverter.class);
		elasticsearchEntityFactory = new ElasticsearchEntityFactory(entityManager, entityToSourceConverter);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void ElasticsearchEntityFactory()
	{
		new ElasticsearchEntityFactory(null, null);
	}

	@Test
	public void createEntityMetaDataEntity()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Entity entity = mock(Entity.class);
		Map<String, Object> source = new HashMap<>();
		when(entityToSourceConverter.convert(entity, entityMeta)).thenReturn(source);
		assertEquals(elasticsearchEntityFactory.create(entityMeta, entity), source);
	}
}

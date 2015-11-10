package org.molgenis.data.elasticsearch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.elasticsearch.index.SourceToEntityConverter;
import org.molgenis.data.support.PartialEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticsearchEntityFactoryTest
{
	private EntityManager entityManager;
	private SourceToEntityConverter sourceToEntityConverter;
	private EntityToSourceConverter entityToSourceConverter;
	private ElasticsearchEntityFactory elasticsearchEntityFactory;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityManager = mock(EntityManager.class);
		sourceToEntityConverter = mock(SourceToEntityConverter.class);
		entityToSourceConverter = mock(EntityToSourceConverter.class);
		elasticsearchEntityFactory = new ElasticsearchEntityFactory(entityManager, sourceToEntityConverter,
				entityToSourceConverter);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void ElasticsearchEntityFactory()
	{
		new ElasticsearchEntityFactory(null, null, null);
	}

	@Test
	public void createEntityMetaDataMapStringObjectFetch()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Map<String, Object> source = new HashMap<String, Object>();
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		PartialEntity partialEntity = mock(PartialEntity.class);
		when(sourceToEntityConverter.convert(source, entityMeta)).thenReturn(entity);
		when(entityManager.createEntityForPartialEntity(entity, fetch)).thenReturn(partialEntity);
		assertEquals(elasticsearchEntityFactory.create(entityMeta, source, fetch), partialEntity);
	}

	@Test
	public void createEntityMetaDataMapStringObjectNoFetch()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Map<String, Object> source = new HashMap<String, Object>();
		Fetch fetch = null;
		Entity entity = mock(Entity.class);
		when(sourceToEntityConverter.convert(source, entityMeta)).thenReturn(entity);
		assertEquals(elasticsearchEntityFactory.create(entityMeta, source, fetch), entity);
		verifyNoMoreInteractions(entityManager);
	}

	@Test
	public void createEntityMetaDataEntity()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		Entity entity = mock(Entity.class);
		Map<String, Object> source = new HashMap<String, Object>();
		when(entityToSourceConverter.convert(entity, entityMeta)).thenReturn(source);
		assertEquals(elasticsearchEntityFactory.create(entityMeta, entity), source);
	}

	@Test
	public void getEntityManager()
	{
		assertEquals(entityManager, elasticsearchEntityFactory.getEntityManager());
	}
}

package org.molgenis.data.elasticsearch;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticsearchRepositoryDecoratorTest
{
	private ElasticsearchRepositoryDecorator elasticSearchRepository;
	private ElasticSearchService elasticSearchService;
	private CrudRepository repository;
	private EntityMetaData repositoryEntityMetaData;
	private String entityName;
	private String idAttrName;

	@BeforeMethod
	public void setUp() throws IOException
	{
		elasticSearchService = mock(ElasticSearchService.class);
		repository = mock(CrudRepository.class);
		entityName = "";
		repositoryEntityMetaData = mock(EntityMetaData.class);
		when(repositoryEntityMetaData.getName()).thenReturn(entityName);
		idAttrName = "id";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(repositoryEntityMetaData.getIdAttribute()).thenReturn(idAttr);
		when(repository.getEntityMetaData()).thenReturn(repositoryEntityMetaData);
		elasticSearchRepository = new ElasticsearchRepositoryDecorator(repository, elasticSearchService);
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ElasticSearchRepository()
	{
		new ElasticsearchRepositoryDecorator(null, null);
	}

	@Test
	public void addEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		elasticSearchRepository.add(entity);
		verify(repository).add(entity);
		ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
		verify(elasticSearchService).index(argument.capture(), eq(repositoryEntityMetaData), eq(IndexingMode.ADD));

	}

	@Test
	public void addIterableextendsEntity()
	{
		List<Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));
		elasticSearchRepository.add(entities);
		verify(repository).add(entities);
		verify(elasticSearchService).index(Matchers.<Iterable<Entity>> any(), eq(repositoryEntityMetaData),
				eq(IndexingMode.ADD));
	}

	@Test
	public void aggregate()
	{
		when(elasticSearchRepository.getName()).thenReturn("entity");
		AttributeMetaData xAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("xAttr").getMock();
		AttributeMetaData yAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("yAttr").getMock();
		AttributeMetaData distinctAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("distinctAttr")
				.getMock();
		Query q = mock(Query.class);
		AggregateQuery aggregateQuery = new AggregateQueryImpl().attrX(xAttr).attrY(yAttr).attrDistinct(distinctAttr)
				.query(q);

		elasticSearchRepository.aggregate(aggregateQuery);
		verify(elasticSearchService).aggregate(aggregateQuery, repositoryEntityMetaData);
	}

	@Test
	public void clearCache()
	{
		elasticSearchRepository.clearCache();
		verify(repository).clearCache();
	}

	@Test
	public void close() throws IOException
	{
		elasticSearchRepository.close();
		verify(repository, times(0)).close(); // do not close repository
	}

	@Test
	public void count()
	{
		elasticSearchRepository.count();
		verify(elasticSearchService).count(repositoryEntityMetaData);
	}

	@Test
	public void countQuery()
	{
		Query q = mock(Query.class);
		elasticSearchRepository.count(q);
		verify(elasticSearchService).count(q, repositoryEntityMetaData);
	}

	@Test
	public void deleteEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		elasticSearchRepository.delete(entity);
		verify(repository).delete(entity);
		ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
		verify(elasticSearchService).delete(argument.capture(), eq(repositoryEntityMetaData));
		assertEquals(argument.getValue().get(idAttrName), entityName + id);
	}

	@Test
	public void deleteIterableextendsEntity()
	{
		List<Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));
		elasticSearchRepository.delete(entities);
		verify(repository).delete(entities);
		verify(elasticSearchService).delete(Matchers.<Iterable<Entity>> any(), eq(repositoryEntityMetaData));
	}

	@Test
	public void deleteAll()
	{
		elasticSearchRepository.deleteAll();
		verify(repository).deleteAll();
		verify(elasticSearchService).delete(repositoryEntityMetaData.getName());
	}

	@Test
	public void deleteByIdObject()
	{
		Object id = "0";
		elasticSearchRepository.deleteById(id);
		verify(repository).deleteById(id);
		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(elasticSearchService).deleteById(argument.capture(), eq(repositoryEntityMetaData));
		assertEquals(argument.getValue(), entityName + id.toString());
	}

	@Test
	public void deleteByIdIterableObject()
	{
		List<Object> ids = Arrays.asList(mock(Object.class), mock(Object.class));
		elasticSearchRepository.deleteById(ids);
		verify(repository).deleteById(ids);
		verify(elasticSearchService).deleteById(Matchers.<Iterable<String>> any(), eq(repositoryEntityMetaData));
	}

	// FIXME
	// @Test
	// public void findAllQuery()
	// {
	// Query q = mock(Query.class);
	// String id0 = "0";
	// String id1 = "1";
	// List<String> ids = Arrays.<String> asList(entityName + id0, entityName + id1);
	// when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
	// elasticSearchRepository.findAll(q);
	// verify(repository).findAll(Matchers.<Iterable<Object>> any());
	// }

	// FIXME
	// @Test
	// public void findAllQuery_noResults()
	// {
	// Query q = mock(Query.class);
	// List<String> ids = Collections.emptyList();
	// when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
	// assertEquals(Iterables.size(elasticSearchRepository.findAll(q)), 0);
	// }

	// FIXME
	// @Test
	// public void findAllQueryClassE()
	// {
	// Query q = mock(Query.class);
	// Class<? extends Entity> clazz = new MapEntity().getClass();
	// String id0 = "0";
	// String id1 = "1";
	// List<String> ids = Arrays.<String> asList(id0, id1);
	// when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
	// elasticSearchRepository.findAll(q, clazz);
	// verify(repository).findAll(Matchers.<Iterable<Object>> any(), eq(clazz));
	// }

	@Test
	public void findAllIterableObject()
	{
		List<Object> ids = Arrays.asList(mock(Object.class), mock(Object.class));
		elasticSearchRepository.findAll(ids);
		verify(elasticSearchService).get(ids, repositoryEntityMetaData);
	}

	@Test
	public void findAllIterableObjectClassE()
	{
		List<Object> ids = Arrays.asList(mock(Object.class), mock(Object.class));
		Class<? extends Entity> clazz = new MapEntity().getClass();
		elasticSearchRepository.findAll(ids, clazz);
		verify(elasticSearchService).get(ids, repositoryEntityMetaData);
	}

	@Test
	public void findOneQuery()
	{
		Query q = mock(Query.class);
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(
				Arrays.<Entity> asList(entity0, entity1));
		elasticSearchRepository.findOne(q);
		verify(elasticSearchService).search(q, repositoryEntityMetaData);
	}

	@Test
	public void findOneQuery_noResults()
	{
		Query q = mock(Query.class);
		List<Entity> entities = Collections.emptyList();
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(entities);
		assertNull(elasticSearchRepository.findOne(q));
	}

	@Test
	public void findOneObject()
	{
		Object id = mock(Object.class);
		elasticSearchRepository.findOne(id);
		verify(elasticSearchService).get(id, repositoryEntityMetaData);
	}

	@Test
	public void findOneObjectClassE()
	{
		Object id = mock(Object.class);
		Class<? extends Entity> clazz = new MapEntity().getClass();
		elasticSearchRepository.findOne(id, clazz);
		verify(elasticSearchService).get(id, repositoryEntityMetaData);
	}

	// FIXME
	// @Test
	// public void findOneQueryClassE()
	// {
	// Query q = mock(Query.class);
	// Class<? extends Entity> clazz = new MapEntity().getClass();
	// String id0 = "0";
	// String id1 = "1";
	// List<String> ids = Arrays.<String> asList(entityName + id0, entityName + id1);
	// when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
	// elasticSearchRepository.findOne(q, clazz);
	// verify(repository).findOne(id0, clazz);
	// }

	// FIXME
	// @Test
	// public void findOneQueryClassE_noResults()
	// {
	// Query q = mock(Query.class);
	// Class<? extends Entity> clazz = new MapEntity().getClass();
	// List<String> ids = Collections.emptyList();
	// when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
	// assertNull(elasticSearchRepository.findOne(q, clazz));
	// }

	@Test
	public void flush()
	{
		elasticSearchRepository.flush();
		verify(repository).flush();
		verify(elasticSearchService).flush();
	}

	@Test
	public void getEntityMetaData()
	{
		elasticSearchRepository.getEntityMetaData();
		verify(repository).getEntityMetaData();
	}

	@Test
	public void getName()
	{
		assertEquals(elasticSearchRepository.getName(), repositoryEntityMetaData.getName());
	}

	@Test
	public void getUrl()
	{
		when(repositoryEntityMetaData.getName()).thenReturn("entity");
		assertEquals(elasticSearchRepository.getUrl(), "elasticsearch://entity/");
	}

	// FIXME
	// @Test
	// public void iteratorClassE()
	// {
	// Class<? extends Entity> clazz = new MapEntity().getClass();
	// elasticSearchRepository.iterator(clazz);
	// verify(repository).iterator(clazz);
	// }
	//
	// @Test
	// public void iterator()
	// {
	// elasticSearchRepository.iterator();
	// when(elasticSearchService.search(new QueryImpl(), repositoryEntityMetaData)).thenReturn(
	// Collections.<Entity> emptyList());
	// verify(elasticSearchService).search(new QueryImpl(), repositoryEntityMetaData);
	// }

	@Test
	public void updateEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		elasticSearchRepository.update(entity);
		verify(repository).update(entity);
		ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
		verify(elasticSearchService).index(argument.capture(), eq(repositoryEntityMetaData), eq(IndexingMode.UPDATE));
		assertEquals(argument.getValue().get(idAttrName), entityName + id);
	}

	@Test
	public void updateIterableextendsEntity()
	{
		List<Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));
		elasticSearchRepository.update(entities);
		verify(repository).update(entities);
		verify(elasticSearchService).index(Matchers.<Iterable<Entity>> any(), eq(repositoryEntityMetaData),
				eq(IndexingMode.UPDATE));
	}

	@Test
	public void rebuildIndex()
	{
		elasticSearchRepository.rebuildIndex();
		verify(elasticSearchService).rebuildIndex(repository, repositoryEntityMetaData);
	}
}

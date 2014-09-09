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

import org.elasticsearch.common.collect.Iterables;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.support.MapEntity;
import org.molgenis.elasticsearch.ElasticSearchService;
import org.molgenis.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
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
		Query q = mock(Query.class);
		SearchRequest searchRequest = new SearchRequest("entity", q, Collections.<String> emptyList(), xAttr, yAttr);
		AggregateResult aggregate = mock(AggregateResult.class);
		SearchResult searchResult = when(mock(SearchResult.class).getAggregate()).thenReturn(aggregate).getMock();
		when(elasticSearchService.search(searchRequest)).thenReturn(searchResult);
		assertEquals(elasticSearchRepository.aggregate(xAttr, yAttr, q), aggregate);
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
		verify(repository).count();
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
		verify(elasticSearchService).delete(repositoryEntityMetaData);
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

	@Test
	public void findAllQuery()
	{
		Query q = mock(Query.class);
		String id0 = "0";
		String id1 = "1";
		List<String> ids = Arrays.<String> asList(entityName + id0, entityName + id1);
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
		elasticSearchRepository.findAll(q);
		verify(repository).findAll(Matchers.<Iterable<Object>> any());
	}

	@Test
	public void findAllQuery_noResults()
	{
		Query q = mock(Query.class);
		List<String> ids = Collections.emptyList();
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
		assertEquals(Iterables.size(elasticSearchRepository.findAll(q)), 0);
	}

	@Test
	public void findAllQueryClassE()
	{
		Query q = mock(Query.class);
		Class<? extends Entity> clazz = new MapEntity().getClass();
		String id0 = "0";
		String id1 = "1";
		List<String> ids = Arrays.<String> asList(id0, id1);
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
		elasticSearchRepository.findAll(q, clazz);
		verify(repository).findAll(Matchers.<Iterable<Object>> any(), eq(clazz));
	}

	@Test
	public void findAllIterableObject()
	{
		List<Object> ids = Arrays.asList(mock(Object.class), mock(Object.class));
		elasticSearchRepository.findAll(ids);
		verify(repository).findAll(ids);
	}

	@Test
	public void findAllIterableObjectClassE()
	{
		List<Object> ids = Arrays.asList(mock(Object.class), mock(Object.class));
		Class<? extends Entity> clazz = new MapEntity().getClass();
		elasticSearchRepository.findAll(ids, clazz);
		verify(repository).findAll(ids, clazz);
	}

	@Test
	public void findOneQuery()
	{
		Query q = mock(Query.class);
		String id0 = "0";
		String id1 = "1";
		List<String> ids = Arrays.<String> asList(entityName + id0, entityName + id1);
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
		elasticSearchRepository.findOne(q);
		verify(repository).findOne(id0);
	}

	@Test
	public void findOneQuery_noResults()
	{
		Query q = mock(Query.class);
		List<String> ids = Collections.emptyList();
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
		assertNull(elasticSearchRepository.findOne(q));
	}

	@Test
	public void findOneObject()
	{
		Object id = mock(Object.class);
		elasticSearchRepository.findOne(id);
		verify(repository).findOne(id);
	}

	@Test
	public void findOneObjectClassE()
	{
		Object id = mock(Object.class);
		Class<? extends Entity> clazz = new MapEntity().getClass();
		elasticSearchRepository.findOne(id, clazz);
		verify(repository).findOne(id, clazz);
	}

	@Test
	public void findOneQueryClassE()
	{
		Query q = mock(Query.class);
		Class<? extends Entity> clazz = new MapEntity().getClass();
		String id0 = "0";
		String id1 = "1";
		List<String> ids = Arrays.<String> asList(entityName + id0, entityName + id1);
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
		elasticSearchRepository.findOne(q, clazz);
		verify(repository).findOne(id0, clazz);
	}

	@Test
	public void findOneQueryClassE_noResults()
	{
		Query q = mock(Query.class);
		Class<? extends Entity> clazz = new MapEntity().getClass();
		List<String> ids = Collections.emptyList();
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(ids);
		assertNull(elasticSearchRepository.findOne(q, clazz));
	}

	@Test
	public void flush()
	{
		elasticSearchRepository.flush();
		verify(repository).flush();
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
		elasticSearchRepository.getName();
		verify(repository).getName();
	}

	@Test
	public void getRepository()
	{
		assertEquals(elasticSearchRepository.getRepository(), repository);
	}

	@Test
	public void getUrl()
	{
		when(repository.getName()).thenReturn("entity");
		assertEquals(elasticSearchRepository.getUrl(), "elasticsearch://entity/");
	}

	@Test
	public void iteratorClassE()
	{
		Class<? extends Entity> clazz = new MapEntity().getClass();
		elasticSearchRepository.iterator(clazz);
		verify(repository).iterator(clazz);
	}

	@Test
	public void iterator()
	{
		elasticSearchRepository.iterator();
		verify(repository).iterator();
	}

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

}

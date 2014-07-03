package org.molgenis.data.elasticsearch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.molgenis.data.AggregateResult;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.ElasticSearchService;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticSearchRepositoryTest
{
	private ElasticSearchRepository elasticSearchRepository;
	private ElasticSearchService elasticSearchService;
	private CrudRepository repository;

	@BeforeMethod
	public void setUp() throws IOException
	{
		elasticSearchService = mock(ElasticSearchService.class);
		repository = mock(CrudRepository.class);
		elasticSearchRepository = new ElasticSearchRepository(elasticSearchService, repository);
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ElasticSearchRepository()
	{
		new ElasticSearchRepository(null, null);
	}

	@Test
	public void addEntity()
	{
		Entity entity = mock(Entity.class);
		elasticSearchRepository.add(entity);
		verify(repository).add(entity);
		// TODO verify index update
	}

	@Test
	public void addIterableextendsEntity()
	{
		List<Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));
		elasticSearchRepository.add(entities);
		verify(repository).add(entities);
		// TODO verify index update
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
		verify(repository).count(new QueryImpl());
	}

	@Test
	public void countQuery()
	{
		Query q = mock(Query.class);
		elasticSearchRepository.count(q);
		verify(repository).count(q);
	}

	@Test
	public void deleteEntity()
	{
		Entity entity = mock(Entity.class);
		elasticSearchRepository.delete(entity);
		verify(repository).delete(entity);
		// TODO verify index update
	}

	@Test
	public void deleteIterableextendsEntity()
	{
		List<Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));
		elasticSearchRepository.delete(entities);
		verify(repository).delete(entities);
		// TODO verify index update
	}

	@Test
	public void deleteAll()
	{
		elasticSearchRepository.deleteAll();
		verify(repository).deleteAll();
		// TODO verify index update
	}

	@Test
	public void deleteByIdObject()
	{
		Object id = mock(Object.class);
		elasticSearchRepository.deleteById(id);
		verify(repository).deleteById(id);
		// TODO verify index update
	}

	@Test
	public void deleteByIdIterableObject()
	{
		List<Object> ids = Arrays.asList(mock(Object.class), mock(Object.class));
		elasticSearchRepository.deleteById(ids);
		verify(repository).deleteById(ids);
		// TODO verify index update
	}

	@Test
	public void findAllQuery()
	{
		Query q = mock(Query.class);
		elasticSearchRepository.findAll(q);
		verify(repository).findAll(q);
	}

	@Test
	public void findAllQueryClassE()
	{
		Query q = mock(Query.class);
		Class<? extends Entity> clazz = new MapEntity().getClass();
		elasticSearchRepository.findAll(q, clazz);
		verify(repository).findAll(q, clazz);
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
		elasticSearchRepository.findOne(q);
		verify(repository).findOne(q);
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
		elasticSearchRepository.findOne(q, clazz);
		verify(repository).findOne(q, clazz);
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
		Entity entity = mock(Entity.class);
		elasticSearchRepository.update(entity);
		verify(repository).update(entity);
		// TODO verify index update
	}

	@Test
	public void updateIterableextendsEntity()
	{
		List<Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));
		elasticSearchRepository.update(entities);
		verify(repository).update(entities);
		// TODO verify index update
	}

	@Test
	public void updateListextendsEntityDatabaseActionString()
	{
		String keyName = "key";
		DatabaseAction dbAction = DatabaseAction.UPDATE_IGNORE_MISSING;
		List<? extends Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));
		elasticSearchRepository.update(entities, dbAction, keyName);
		verify(repository).update(entities, dbAction, keyName);
		// TODO verify index update
	}
}

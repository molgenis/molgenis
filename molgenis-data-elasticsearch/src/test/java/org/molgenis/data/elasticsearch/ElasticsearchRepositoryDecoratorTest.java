package org.molgenis.data.elasticsearch;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.Sort;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.support.AggregateQueryImpl;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ElasticsearchRepositoryDecoratorTest
{
	private ElasticsearchRepositoryDecorator elasticsearchRepositoryDecorator;
	private ElasticsearchService elasticSearchService;
	private Repository decoratedRepo;
	private EntityMetaData repositoryEntityMetaData;
	private String entityName;
	private String idAttrName;

	@BeforeMethod
	public void setUp() throws IOException
	{
		elasticSearchService = mock(ElasticsearchService.class);
		decoratedRepo = mock(Repository.class);
		entityName = "";
		repositoryEntityMetaData = mock(EntityMetaData.class);
		when(repositoryEntityMetaData.getName()).thenReturn(entityName);
		idAttrName = "id";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(repositoryEntityMetaData.getIdAttribute()).thenReturn(idAttr);
		when(decoratedRepo.getEntityMetaData()).thenReturn(repositoryEntityMetaData);
		when(decoratedRepo.getCapabilities()).thenReturn(Collections.singleton(RepositoryCapability.QUERYABLE));
		elasticsearchRepositoryDecorator = new ElasticsearchRepositoryDecorator(decoratedRepo, elasticSearchService);
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
		elasticsearchRepositoryDecorator.add(entity);
		verify(decoratedRepo).add(entity);
		ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
		verify(elasticSearchService).index(argument.capture(), eq(repositoryEntityMetaData), eq(IndexingMode.ADD));

	}

	@Test
	public void addStream()
	{
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < 1100; ++i)
		{
			entities.add(mock(Entity.class));
		}
		elasticsearchRepositoryDecorator.add(entities.stream());
		verify(decoratedRepo, times(2)).add(Matchers.<Stream<Entity>> any());
		verify(elasticSearchService, times(2)).index(Matchers.<Stream<Entity>> any(), eq(repositoryEntityMetaData),
				eq(IndexingMode.ADD));
	}

	@Test
	public void aggregate()
	{
		when(elasticsearchRepositoryDecorator.getName()).thenReturn("entity");
		AttributeMetaData xAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("xAttr").getMock();
		AttributeMetaData yAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("yAttr").getMock();
		AttributeMetaData distinctAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("distinctAttr")
				.getMock();
		Query q = mock(Query.class);
		AggregateQuery aggregateQuery = new AggregateQueryImpl().attrX(xAttr).attrY(yAttr).attrDistinct(distinctAttr)
				.query(q);

		elasticsearchRepositoryDecorator.aggregate(aggregateQuery);
		verify(elasticSearchService).aggregate(aggregateQuery, repositoryEntityMetaData);
	}

	@Test
	public void clearCache()
	{
		elasticsearchRepositoryDecorator.clearCache();
		verify(decoratedRepo).clearCache();
	}

	@Test
	public void close() throws IOException
	{
		elasticsearchRepositoryDecorator.close();
		verify(decoratedRepo, times(0)).close(); // do not close repository
	}

	@Test
	public void count()
	{
		elasticsearchRepositoryDecorator.count();
		verify(elasticSearchService).count(repositoryEntityMetaData);
	}

	@Test
	public void countQuery()
	{
		Query q = mock(Query.class);
		elasticsearchRepositoryDecorator.count(q);
		verify(elasticSearchService).count(q, repositoryEntityMetaData);
	}

	@Test
	public void deleteEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		elasticsearchRepositoryDecorator.delete(entity);
		verify(decoratedRepo).delete(entity);
		ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
		verify(elasticSearchService).delete(argument.capture(), eq(repositoryEntityMetaData));
		assertEquals(argument.getValue().get(idAttrName), entityName + id);
	}

	@Test
	public void deleteStream()
	{
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < 1100; ++i)
		{
			entities.add(mock(Entity.class));
		}
		elasticsearchRepositoryDecorator.delete(entities.stream());
		verify(decoratedRepo, times(2)).delete(Matchers.<Stream<Entity>> any());
		verify(elasticSearchService, times(2)).delete(Matchers.<Stream<Entity>> any(), eq(repositoryEntityMetaData));
	}

	@Test
	public void deleteAll()
	{
		elasticsearchRepositoryDecorator.deleteAll();
		verify(decoratedRepo).deleteAll();
		verify(elasticSearchService).delete(repositoryEntityMetaData.getName());
	}

	@Test
	public void deleteByIdObject()
	{
		Object id = "0";
		elasticsearchRepositoryDecorator.deleteById(id);
		verify(decoratedRepo).deleteById(id);
		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(elasticSearchService).deleteById(argument.capture(), eq(repositoryEntityMetaData));
		assertEquals(argument.getValue(), entityName + id.toString());
	}

	@Test
	public void findOneQuery()
	{
		Query q = mock(Query.class);
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		when(elasticSearchService.search(q, repositoryEntityMetaData))
				.thenReturn(Arrays.<Entity> asList(entity0, entity1));
		elasticsearchRepositoryDecorator.findOne(q);
		verify(elasticSearchService).search(q, repositoryEntityMetaData);
	}

	@Test
	public void findOneQueryIsIdQuery()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Query q = mock(Query.class);
		QueryRule queryRule = new QueryRule(idAttrName, Operator.EQUALS, id);
		when(q.getRules()).thenReturn(Arrays.asList(queryRule));
		when(q.getFetch()).thenReturn(fetch);

		Entity entity = mock(Entity.class);
		when(decoratedRepo.findOne(id, fetch)).thenReturn(entity);
		assertEquals(elasticsearchRepositoryDecorator.findOne(q), entity);
		verify(decoratedRepo, times(1)).findOne(id, fetch);
	}

	@Test
	public void findOneQuery_noResults()
	{
		Query q = mock(Query.class);
		List<Entity> entities = Collections.emptyList();
		when(elasticSearchService.search(q, repositoryEntityMetaData)).thenReturn(entities);
		assertNull(elasticsearchRepositoryDecorator.findOne(q));
	}

	@Test
	public void findOneObject()
	{
		Object id = mock(Object.class);
		elasticsearchRepositoryDecorator.findOne(id);
		verify(decoratedRepo).findOne(id);
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = mock(Object.class);
		Fetch fetch = new Fetch();

		Entity entity = mock(Entity.class);
		when(decoratedRepo.findOne(id, fetch)).thenReturn(entity);
		assertEquals(elasticsearchRepositoryDecorator.findOne(id, fetch), entity);
		verify(decoratedRepo, times(1)).findOne(id, fetch);
		verifyNoMoreInteractions(decoratedRepo);
	}

	@Test
	public void flush()
	{
		elasticsearchRepositoryDecorator.flush();
		verify(decoratedRepo).flush();
		verify(elasticSearchService).flush();
	}

	@Test
	public void getEntityMetaData()
	{
		elasticsearchRepositoryDecorator.getEntityMetaData();
		verify(decoratedRepo).getEntityMetaData();
	}

	@Test
	public void getName()
	{
		assertEquals(elasticsearchRepositoryDecorator.getName(), repositoryEntityMetaData.getName());
	}

	@Test
	public void updateEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		elasticsearchRepositoryDecorator.update(entity);
		verify(decoratedRepo).update(entity);
		ArgumentCaptor<Entity> argument = ArgumentCaptor.forClass(Entity.class);
		verify(elasticSearchService).index(argument.capture(), eq(repositoryEntityMetaData), eq(IndexingMode.UPDATE));
		assertEquals(argument.getValue().get(idAttrName), entityName + id);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < 1100; ++i)
		{
			entities.add(mock(Entity.class));
		}
		elasticsearchRepositoryDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> decoratedRepoCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo, times(2)).update(decoratedRepoCaptor.capture());
		List<Stream<Entity>> decoratedRepoValues = decoratedRepoCaptor.getAllValues();
		assertEquals(decoratedRepoValues.size(), 2);
		assertEquals(decoratedRepoValues.get(0).collect(Collectors.toList()), entities.subList(0, 1000));
		assertEquals(decoratedRepoValues.get(1).collect(Collectors.toList()), entities.subList(1000, 1100));

		ArgumentCaptor<Stream<Entity>> elasticSearchServiceCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(elasticSearchService, times(2)).index(elasticSearchServiceCaptor.capture(), eq(repositoryEntityMetaData),
				eq(IndexingMode.UPDATE));
		List<Stream<Entity>> elasticSearchServiceValues = elasticSearchServiceCaptor.getAllValues();
		assertEquals(elasticSearchServiceValues.size(), 2);
		assertEquals(elasticSearchServiceValues.get(0).collect(Collectors.toList()), entities.subList(0, 1000));
		assertEquals(elasticSearchServiceValues.get(1).collect(Collectors.toList()), entities.subList(1000, 1100));
	}

	@Test
	public void rebuildIndex()
	{
		elasticsearchRepositoryDecorator.rebuildIndex();
		verify(elasticSearchService).rebuildIndex(decoratedRepo, repositoryEntityMetaData);
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = elasticsearchRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamQueryNoFetch()
	{
		QueryImpl q = new QueryImpl();
		elasticsearchRepositoryDecorator.findAll(q);
		verify(decoratedRepo).stream();
	}

	@Test
	public void findAllStreamQueryEmptyFetch()
	{
		QueryImpl q = new QueryImpl();
		Fetch fetch = mock(Fetch.class);
		q.setFetch(fetch);
		elasticsearchRepositoryDecorator.findAll(q);
		verify(decoratedRepo).stream(fetch);
	}

	@Test
	public void findAllStreamQueryFetchWithOffset()
	{
		QueryImpl q = new QueryImpl();
		Fetch fetch = mock(Fetch.class);
		q.setFetch(fetch);
		q.setOffset(1);
		elasticsearchRepositoryDecorator.findAll(q);
		verify(decoratedRepo).findAll(q);
	}

	@Test
	public void findAllStreamQueryFetchWithOffsetAndPageSize()
	{
		QueryImpl q = new QueryImpl();
		Fetch fetch = mock(Fetch.class);
		q.setFetch(fetch);
		q.setOffset(0);
		q.setPageSize(20);
		elasticsearchRepositoryDecorator.findAll(q);
		verify(decoratedRepo).findAll(q);
	}

	@Test
	public void findAllStreamQueryFetchWithSort()
	{
		QueryImpl q = new QueryImpl();
		Fetch fetch = mock(Fetch.class);
		q.setFetch(fetch);
		q.setSort(mock(Sort.class));
		elasticsearchRepositoryDecorator.findAll(q);
		verify(decoratedRepo).findAll(q);
	}

	@Test
	public void findAllStreamQueryFetchWithOffsetAndPageSizeAndSort()
	{
		QueryImpl q = new QueryImpl();
		Fetch fetch = mock(Fetch.class);
		q.setFetch(fetch);
		q.setOffset(1);
		q.setPageSize(20);
		q.setSort(mock(Sort.class));
		q.not();
		elasticsearchRepositoryDecorator.findAll(q);
		verify(decoratedRepo, never()).stream();
		verify(decoratedRepo, never()).stream(fetch);
		verify(decoratedRepo, never()).findAll(q);
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = elasticsearchRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findAllStreamInQueryQueryableRepo()
	{
		QueryImpl q = new QueryImpl();
		q.in("field", Arrays.asList("id0", "id1"));
		elasticsearchRepositoryDecorator.findAll(q);
		verify(decoratedRepo, times(1)).findAll(q);
	}

	@Test
	public void findAllStreamInQueryNonQueryableRepo()
	{
		when(decoratedRepo.getCapabilities()).thenReturn(Collections.emptySet());

		QueryImpl q = new QueryImpl();
		q.in("field", Arrays.asList("id0", "id1"));
		elasticsearchRepositoryDecorator.findAll(q);
		verify(decoratedRepo, never()).findAll(q);
	}

	@Test
	public void streamFetch()
	{
		Fetch fetch = new Fetch();
		elasticsearchRepositoryDecorator.stream(fetch);
		verify(decoratedRepo, times(1)).stream(fetch);
	}
}

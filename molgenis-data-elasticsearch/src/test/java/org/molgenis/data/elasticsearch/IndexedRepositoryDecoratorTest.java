package org.molgenis.data.elasticsearch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.AND;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH;
import static org.molgenis.data.QueryRule.Operator.IN;
import static org.molgenis.data.QueryRule.Operator.LESS;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.VALIDATE_NOTNULL_CONSTRAINT;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.AggregateQueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class IndexedRepositoryDecoratorTest
{
	private IndexedRepositoryDecorator indexedRepositoryDecorator;
	private ElasticsearchService elasticSearchService;
	private Repository<Entity> decoratedRepo;
	private EntityMetaData repositoryEntityMetaData;
	private String entityName;
	private String idAttrName;
	private Query<Entity> query;
	private Query<Entity> unsupportedQuery;
	private List<QueryRule> queryRules;
	private List<QueryRule> unsupportedQueryRules;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws IOException
	{
		elasticSearchService = mock(ElasticsearchService.class);
		decoratedRepo = mock(Repository.class);
		entityName = "entity";
		repositoryEntityMetaData = mock(EntityMetaData.class);
		when(repositoryEntityMetaData.getName()).thenReturn(entityName);
		idAttrName = "id";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();

		when(idAttr.getExpression()).thenReturn(null);
		when(repositoryEntityMetaData.getIdAttribute()).thenReturn(idAttr);
		when(decoratedRepo.getEntityMetaData()).thenReturn(repositoryEntityMetaData);
		when(decoratedRepo.getName()).thenReturn("entity");
		when(decoratedRepo.getCapabilities()).thenReturn(EnumSet.of(QUERYABLE, MANAGABLE, VALIDATE_NOTNULL_CONSTRAINT));
		when(decoratedRepo.getQueryOperators()).thenReturn(EnumSet.of(IN, LESS, EQUALS, AND, OR));
		indexedRepositoryDecorator = new IndexedRepositoryDecorator(decoratedRepo, elasticSearchService);

		when(repositoryEntityMetaData.getAtomicAttributes()).thenReturn(Lists.newArrayList(idAttr));

		query = mock(Query.class);
		QueryRule rule1 = mock(QueryRule.class);
		QueryRule rule2 = mock(QueryRule.class);
		when(rule1.getOperator()).thenReturn(IN);
		when(rule2.getOperator()).thenReturn(EQUALS);
		queryRules = Lists.newArrayList(rule1, rule2);
		when(query.getRules()).thenReturn(queryRules);

		unsupportedQuery = mock(Query.class);
		QueryRule unsupportedRule = mock(QueryRule.class);
		when(unsupportedRule.getOperator()).thenReturn(FUZZY_MATCH);
		unsupportedQueryRules = Lists.newArrayList(rule1, rule2, unsupportedRule);
		when(unsupportedQuery.getRules()).thenReturn(unsupportedQueryRules);

	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = NullPointerException.class)
	public void ElasticSearchRepository()
	{
		new IndexedRepositoryDecorator(null, null);
	}

	@Test
	public void addEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		indexedRepositoryDecorator.add(entity);
		verify(decoratedRepo).add(entity);

		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void addStream()
	{
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < 1100; ++i)
		{
			entities.add(mock(Entity.class));
		}
		indexedRepositoryDecorator.add(entities.stream());
		verify(decoratedRepo, times(2)).add(Matchers.<Stream<Entity>>any());

		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void aggregate()
	{
		when(indexedRepositoryDecorator.getName()).thenReturn("entity");
		AttributeMetaData xAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("xAttr").getMock();
		AttributeMetaData yAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("yAttr").getMock();
		AttributeMetaData distinctAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("distinctAttr")
				.getMock();

		@SuppressWarnings("unchecked") Query<Entity> q = mock(Query.class);
		AggregateQuery aggregateQuery = new AggregateQueryImpl().attrX(xAttr).attrY(yAttr).attrDistinct(distinctAttr)
				.query(q);

		indexedRepositoryDecorator.aggregate(aggregateQuery);
		verify(elasticSearchService).aggregate(aggregateQuery, repositoryEntityMetaData);
	}

	@Test
	public void clearCache()
	{
		indexedRepositoryDecorator.clearCache();
		verify(decoratedRepo).clearCache();
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void close() throws IOException
	{
		indexedRepositoryDecorator.close();
		verify(decoratedRepo).close();
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void count()
	{
		indexedRepositoryDecorator.count();
		verify(decoratedRepo).count();
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void countQuery()
	{
		indexedRepositoryDecorator.count(query);
		verify(decoratedRepo).count(query);
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void countQueryUnsupported()
	{
		indexedRepositoryDecorator.count(unsupportedQuery);
		verify(elasticSearchService).count(unsupportedQuery, repositoryEntityMetaData);
		verify(decoratedRepo, never()).count(unsupportedQuery);
	}

	@Test
	public void deleteEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		indexedRepositoryDecorator.delete(entity);
		verify(decoratedRepo).delete(entity);
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void deleteStream()
	{
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < 1100; ++i)
		{
			entities.add(mock(Entity.class));
		}
		indexedRepositoryDecorator.delete(entities.stream());
		verify(decoratedRepo, times(2)).delete(Matchers.<Stream<Entity>>any());
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void deleteAll()
	{
		indexedRepositoryDecorator.deleteAll();
		verify(decoratedRepo).deleteAll();
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void deleteByIdObject()
	{
		Object id = "0";
		indexedRepositoryDecorator.deleteById(id);
		verify(decoratedRepo).deleteById(id);
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void findOneQuery()
	{
		indexedRepositoryDecorator.findOne(query);
		verify(decoratedRepo).findOne(query);
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void findOneQueryUnsupported()
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		when(elasticSearchService.search(unsupportedQuery, repositoryEntityMetaData))
				.thenReturn(Arrays.<Entity>asList(entity0, entity1));

		indexedRepositoryDecorator.findOne(unsupportedQuery);
		verify(elasticSearchService).search(unsupportedQuery, repositoryEntityMetaData);
		verify(decoratedRepo, never()).findOne(unsupportedQuery);
	}

	@Test
	public void findOneById()
	{
		Object id = mock(Object.class);
		indexedRepositoryDecorator.findOneById(id);
		verify(decoratedRepo).findOneById(id);
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void findOneByIdFetch()
	{
		Object id = mock(Object.class);
		Fetch fetch = new Fetch();

		Entity entity = mock(Entity.class);
		when(decoratedRepo.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(indexedRepositoryDecorator.findOneById(id, fetch), entity);
		verify(decoratedRepo, times(1)).findOneById(id, fetch);
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void flush()
	{
		indexedRepositoryDecorator.flush();
		verify(decoratedRepo).flush();
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void getEntityMetaData()
	{
		assertEquals(indexedRepositoryDecorator.getEntityMetaData(), repositoryEntityMetaData);
	}

	@Test
	public void getName()
	{
		assertEquals(indexedRepositoryDecorator.getName(), repositoryEntityMetaData.getName());
	}

	@Test
	public void updateEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		indexedRepositoryDecorator.update(entity);
		verify(decoratedRepo).update(entity);
		verifyZeroInteractions(elasticSearchService);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		List<Entity> entities = new ArrayList<Entity>();
		for (int i = 0; i < 1100; ++i)
		{
			entities.add(mock(Entity.class));
		}
		indexedRepositoryDecorator.update(entities.stream());

		ArgumentCaptor<Stream<Entity>> decoratedRepoCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(decoratedRepo, times(2)).update(decoratedRepoCaptor.capture());
		List<Stream<Entity>> decoratedRepoValues = decoratedRepoCaptor.getAllValues();
		assertEquals(decoratedRepoValues.size(), 2);
		assertEquals(decoratedRepoValues.get(0).collect(Collectors.toList()), entities.subList(0, 1000));
		assertEquals(decoratedRepoValues.get(1).collect(Collectors.toList()), entities.subList(1000, 1100));

		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void rebuildIndex()
	{
		indexedRepositoryDecorator.rebuildIndex();
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
		Stream<Entity> expectedEntities = indexedRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
		verifyZeroInteractions(elasticSearchService);
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
		Stream<Entity> expectedEntities = indexedRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void findAllQuery()
	{
		indexedRepositoryDecorator.findAll(query);
		verify(decoratedRepo, times(1)).findAll(query);
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void findAllQueryUnsupported()
	{
		indexedRepositoryDecorator.findAll(unsupportedQuery);
		verify(elasticSearchService).searchAsStream(unsupportedQuery, repositoryEntityMetaData);
		verify(decoratedRepo, never()).findAll(unsupportedQuery);
	}

	@Test
	public void streamFetch()
	{
		Fetch fetch = new Fetch();
		indexedRepositoryDecorator.stream(fetch);
		verify(decoratedRepo, times(1)).stream(fetch);
	}

	@Test
	public void iterator()
	{
		indexedRepositoryDecorator.iterator();
		verify(decoratedRepo, times(1)).iterator();
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void getCapabilities()
	{
		assertEquals(indexedRepositoryDecorator.getCapabilities(),
				EnumSet.of(AGGREGATEABLE, QUERYABLE, MANAGABLE, VALIDATE_NOTNULL_CONSTRAINT));
	}

	@Test
	public void getQueryOperators()
	{
		assertEquals(indexedRepositoryDecorator.getQueryOperators(), EnumSet.allOf(Operator.class));
	}

	@Test
	public void query()
	{
		indexedRepositoryDecorator.query();
		verify(decoratedRepo, times(1)).query();
		verifyZeroInteractions(elasticSearchService);
	}

	@Test
	public void addEntityListener()
	{
		EntityListener listener = mock(EntityListener.class);
		indexedRepositoryDecorator.addEntityListener(listener);
		verify(decoratedRepo, times(1)).addEntityListener(listener);
	}

	@Test
	public void removeEntityListener()
	{
		EntityListener listener = mock(EntityListener.class);
		indexedRepositoryDecorator.removeEntityListener(listener);
		verify(decoratedRepo, times(1)).removeEntityListener(listener);
	}

	@Test
	public void unsupportedQueryWithComputedAttributes()
	{
		@SuppressWarnings("unchecked") Query<Entity> q = mock(Query.class);
		QueryRule qRule1 = mock(QueryRule.class);
		QueryRule qRule2 = mock(QueryRule.class);

		when(qRule1.getField()).thenReturn("attr1");
		when(qRule2.getField()).thenReturn("attr2");
		when(qRule1.getOperator()).thenReturn(EQUALS);
		when(qRule2.getOperator()).thenReturn(OR);
		when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
		when(qRule2.getNestedRules()).thenReturn(Collections.emptyList());
		when(q.getRules()).thenReturn(Lists.newArrayList(qRule1, qRule2));

		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		when(repositoryEntityMetaData.getAttribute("attr1")).thenReturn(attr1);
		when(attr1.getExpression()).thenReturn(null);

		AttributeMetaData attr2 = mock(AttributeMetaData.class);
		when(repositoryEntityMetaData.getAttribute("attr2")).thenReturn(attr2);
		when(attr2.getExpression()).thenReturn("${value}");

		indexedRepositoryDecorator.count(q);
		verify(elasticSearchService).count(q, repositoryEntityMetaData);
		verify(decoratedRepo, never()).count(q);
	}
}

package org.molgenis.data.elasticsearch;

import com.google.common.collect.Lists;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.AggregateQueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.molgenis.data.RepositoryCapability.*;
import static org.testng.Assert.assertEquals;

public class IndexedRepositoryDecoratorTest
{
	private IndexedRepositoryDecorator indexedRepositoryDecorator;
	private SearchService searchService;
	private Repository<Entity> decoratedRepo;
	private EntityMetaData repositoryEntityMetaData;
	private String idAttrName;
	private Query<Entity> query;
	private Query<Entity> unsupportedQuery;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws IOException
	{
		searchService = mock(SearchService.class);
		decoratedRepo = mock(Repository.class);
		String entityName = "entity";
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
		indexedRepositoryDecorator = new IndexedRepositoryDecorator(decoratedRepo, searchService);

		when(repositoryEntityMetaData.getAtomicAttributes()).thenReturn(Lists.newArrayList(idAttr));

		query = mock(Query.class);
		QueryRule rule1 = mock(QueryRule.class);
		QueryRule rule2 = mock(QueryRule.class);
		when(rule1.getOperator()).thenReturn(IN);
		when(rule2.getOperator()).thenReturn(EQUALS);
		List<QueryRule> queryRules = Lists.newArrayList(rule1, rule2);
		when(query.getRules()).thenReturn(queryRules);

		unsupportedQuery = mock(Query.class);
		QueryRule unsupportedRule = mock(QueryRule.class);
		when(unsupportedRule.getOperator()).thenReturn(FUZZY_MATCH);
		List<QueryRule> unsupportedQueryRules = Lists.newArrayList(rule1, rule2, unsupportedRule);
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

		verifyZeroInteractions(searchService);
	}

	@Test
	public void addStream()
	{
		Stream<Entity> entities = Stream.empty();
		indexedRepositoryDecorator.add(entities);
		verify(decoratedRepo, times(1)).add(entities);
		verifyZeroInteractions(searchService);
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
		verify(searchService).aggregate(aggregateQuery, repositoryEntityMetaData);
	}

	@Test
	public void close() throws IOException
	{
		indexedRepositoryDecorator.close();
		verify(decoratedRepo).close();
		verifyZeroInteractions(searchService);
	}

	@Test
	public void count()
	{
		indexedRepositoryDecorator.count();
		verify(decoratedRepo).count();
		verifyZeroInteractions(searchService);
	}

	@Test
	public void countQuery()
	{
		indexedRepositoryDecorator.count(query);
		verify(decoratedRepo).count(query);
		verifyZeroInteractions(searchService);
	}

	@Test
	public void countQueryUnsupported()
	{
		indexedRepositoryDecorator.count(unsupportedQuery);
		verify(searchService).count(unsupportedQuery, repositoryEntityMetaData);
		verify(decoratedRepo, never()).count(unsupportedQuery);
	}

	@Test
	public void deleteEntity()
	{
		String id = "id0";
		Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
		indexedRepositoryDecorator.delete(entity);
		verify(decoratedRepo).delete(entity);
		verifyZeroInteractions(searchService);
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.empty();
		indexedRepositoryDecorator.delete(entities);
		verify(decoratedRepo, times(1)).delete(entities);
		verifyZeroInteractions(searchService);
	}

	@Test
	public void deleteAll()
	{
		indexedRepositoryDecorator.deleteAll();
		verify(decoratedRepo).deleteAll();
		verifyZeroInteractions(searchService);
	}

	@Test
	public void deleteByIdObject()
	{
		Object id = "0";
		indexedRepositoryDecorator.deleteById(id);
		verify(decoratedRepo).deleteById(id);
		verifyZeroInteractions(searchService);
	}

	@Test
	public void findOneQuery()
	{
		indexedRepositoryDecorator.findOne(query);
		verify(decoratedRepo).findOne(query);
		verifyZeroInteractions(searchService);
	}

	@Test
	public void findOneQueryUnsupported()
	{
		Entity entity0 = mock(Entity.class);
		when(searchService.findOne(unsupportedQuery, repositoryEntityMetaData)).thenReturn(entity0);

		indexedRepositoryDecorator.findOne(unsupportedQuery);
		verify(searchService).findOne(unsupportedQuery, repositoryEntityMetaData);
		verify(decoratedRepo, never()).findOne(unsupportedQuery);
	}

	@Test
	public void findOneById()
	{
		Object id = mock(Object.class);
		indexedRepositoryDecorator.findOneById(id);
		verify(decoratedRepo).findOneById(id);
		verifyZeroInteractions(searchService);
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
		verifyZeroInteractions(searchService);
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
		verifyZeroInteractions(searchService);
	}

	@Test
	public void updateStream()
	{
		Stream<Entity> entities = Stream.empty();
		indexedRepositoryDecorator.update(entities);
		verify(decoratedRepo, times(1)).update(entities);
		verifyZeroInteractions(searchService);
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
		verifyZeroInteractions(searchService);
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
		verifyZeroInteractions(searchService);
	}

	@Test
	public void findAllQuery()
	{
		indexedRepositoryDecorator.findAll(query);
		verify(decoratedRepo, times(1)).findAll(query);
		verifyZeroInteractions(searchService);
	}

	@Test
	public void findAllQueryUnsupported()
	{
		indexedRepositoryDecorator.findAll(unsupportedQuery);
		verify(searchService).searchAsStream(unsupportedQuery, repositoryEntityMetaData);
		verify(decoratedRepo, never()).findAll(unsupportedQuery);
	}

	@Test
	public void forEachBatched()
	{
		Fetch fetch = new Fetch();
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		indexedRepositoryDecorator.forEachBatched(fetch, consumer, 12);
		verify(decoratedRepo, times(1)).forEachBatched(fetch, consumer, 12);
	}

	@Test
	public void iterator()
	{
		indexedRepositoryDecorator.iterator();
		verify(decoratedRepo, times(1)).iterator();
		verifyZeroInteractions(searchService);
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
		verifyZeroInteractions(searchService);
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
		verify(searchService).count(q, repositoryEntityMetaData);
		verify(decoratedRepo, never()).count(q);
	}
}

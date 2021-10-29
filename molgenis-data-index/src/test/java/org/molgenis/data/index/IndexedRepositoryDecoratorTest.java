package org.molgenis.data.index;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.EnumSet.allOf;
import static java.util.EnumSet.of;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isNull;
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

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.Sort;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.index.exception.UnknownIndexException;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AggregateQueryImpl;

class IndexedRepositoryDecoratorTest {
  private IndexedRepositoryDecorator indexedRepositoryDecorator;
  private SearchService searchService;
  private Repository<Entity> delegateRepository;
  private EntityType repositoryEntityType;
  private String idAttrName;
  private Query<Entity> query;
  private Query<Entity> unsupportedQuery;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() throws IOException {
    searchService = mock(SearchService.class);
    delegateRepository = mock(Repository.class);
    String entityTypeId = "entity";
    repositoryEntityType = mock(EntityType.class);
    when(repositoryEntityType.getId()).thenReturn(entityTypeId);
    when(repositoryEntityType.getLabel()).thenReturn("My entity type");
    idAttrName = "id";
    Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn(idAttrName).getMock();

    when(idAttr.getExpression()).thenReturn(null);
    when(repositoryEntityType.getIdAttribute()).thenReturn(idAttr);
    when(delegateRepository.getEntityType()).thenReturn(repositoryEntityType);
    when(delegateRepository.getName()).thenReturn("entity");
    when(delegateRepository.getCapabilities())
        .thenReturn(EnumSet.of(QUERYABLE, MANAGABLE, VALIDATE_NOTNULL_CONSTRAINT));
    when(delegateRepository.getQueryOperators()).thenReturn(EnumSet.of(IN, LESS, EQUALS, AND, OR));
    IndexJobScheduler indexJobScheduler = mock(IndexJobScheduler.class);
    indexedRepositoryDecorator =
        new IndexedRepositoryDecorator(delegateRepository, searchService, indexJobScheduler);

    when(repositoryEntityType.getAtomicAttributes()).thenReturn(newArrayList(idAttr));

    query = mock(Query.class);
    QueryRule rule1 = mock(QueryRule.class);
    QueryRule rule2 = mock(QueryRule.class);
    when(rule1.getOperator()).thenReturn(IN);
    when(rule2.getOperator()).thenReturn(EQUALS);
    List<QueryRule> queryRules = newArrayList(rule1, rule2);
    when(query.getRules()).thenReturn(queryRules);

    unsupportedQuery = mock(Query.class);
    QueryRule unsupportedRule = mock(QueryRule.class);
    when(unsupportedRule.getOperator()).thenReturn(FUZZY_MATCH);
    List<QueryRule> unsupportedQueryRules = newArrayList(rule1, rule2, unsupportedRule);
    when(unsupportedQuery.getRules()).thenReturn(unsupportedQueryRules);
  }

  @SuppressWarnings("resource")
  @Test
  void indexedRepositoryDecorator() {
    assertThrows(
        NullPointerException.class, () -> new IndexedRepositoryDecorator(null, null, null));
  }

  @Test
  void addEntity() {
    String id = "id0";
    Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
    indexedRepositoryDecorator.add(entity);
    verify(delegateRepository).add(entity);

    verifyZeroInteractions(searchService);
  }

  @Test
  void addStream() {
    Stream<Entity> entities = Stream.empty();
    indexedRepositoryDecorator.add(entities);
    verify(delegateRepository, times(1)).add(entities);
    verifyZeroInteractions(searchService);
  }

  @Test
  void aggregate() {
    when(indexedRepositoryDecorator.getName()).thenReturn("entity");
    Attribute xAttr = when(mock(Attribute.class).getName()).thenReturn("xAttr").getMock();
    Attribute yAttr = when(mock(Attribute.class).getName()).thenReturn("yAttr").getMock();
    Attribute distinctAttr =
        when(mock(Attribute.class).getName()).thenReturn("distinctAttr").getMock();

    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    AggregateQuery aggregateQuery =
        new AggregateQueryImpl().attrX(xAttr).attrY(yAttr).attrDistinct(distinctAttr).query(q);

    indexedRepositoryDecorator.aggregate(aggregateQuery);
    verify(searchService).aggregate(repositoryEntityType, aggregateQuery);
  }

  @Test
  void aggregateUnknownIndexExceptionRecoverable() {
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    AggregateResult aggregateResult = mock(AggregateResult.class);
    when(searchService.aggregate(repositoryEntityType, aggregateQuery))
        .thenThrow(new UnknownIndexException("index"))
        .thenReturn(aggregateResult);

    assertEquals(aggregateResult, indexedRepositoryDecorator.aggregate(aggregateQuery));
    verify(delegateRepository, never()).count(unsupportedQuery);
  }

  @Test
  void aggregateUnknownIndexExceptionUnrecoverable() {
    AggregateQuery aggregateQuery = mock(AggregateQuery.class);
    when(searchService.aggregate(repositoryEntityType, aggregateQuery))
        .thenThrow(new UnknownIndexException("index"));
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> indexedRepositoryDecorator.aggregate(aggregateQuery));
    assertThat(exception.getMessage())
        .containsPattern(
            "Error executing query, index for entity type 'My entity type' with id 'entity' does not exist");
  }

  @Test
  void close() throws IOException {
    indexedRepositoryDecorator.close();
    verify(delegateRepository).close();
    verifyZeroInteractions(searchService);
  }

  @Test
  void count() {
    indexedRepositoryDecorator.count();
    verify(delegateRepository).count();
    verifyZeroInteractions(searchService);
  }

  @Test
  void countQuery() {
    indexedRepositoryDecorator.count(query);
    verify(delegateRepository).count(query);
    verifyZeroInteractions(searchService);
  }

  @Test
  void countQueryUnsupported() {
    indexedRepositoryDecorator.count(unsupportedQuery);
    verify(searchService).count(repositoryEntityType, unsupportedQuery);
    verify(delegateRepository, never()).count(unsupportedQuery);
  }

  @Test
  void countUnknownIndexExceptionRecoverable() {
    when(searchService.count(repositoryEntityType, unsupportedQuery))
        .thenThrow(new UnknownIndexException("index"))
        .thenReturn(5L);

    assertEquals(5L, indexedRepositoryDecorator.count(unsupportedQuery));
    verify(delegateRepository, never()).count(unsupportedQuery);
  }

  @Test
  void countUnknownIndexExceptionUnrecoverable() {
    when(searchService.count(repositoryEntityType, unsupportedQuery))
        .thenThrow(new UnknownIndexException("index"));
    Exception exception =
        assertThrows(
            MolgenisDataException.class, () -> indexedRepositoryDecorator.count(unsupportedQuery));
    assertThat(exception.getMessage())
        .containsPattern(
            "Error executing query, index for entity type 'My entity type' with id 'entity' does not exist");
  }

  @Test
  void deleteEntity() {
    String id = "id0";
    Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
    indexedRepositoryDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
    verifyZeroInteractions(searchService);
  }

  @Test
  void deleteStream() {
    Stream<Entity> entities = Stream.empty();
    indexedRepositoryDecorator.delete(entities);
    verify(delegateRepository, times(1)).delete(entities);
    verifyZeroInteractions(searchService);
  }

  @Test
  void deleteAll() {
    indexedRepositoryDecorator.deleteAll();
    verify(delegateRepository).deleteAll();
    verifyZeroInteractions(searchService);
  }

  @Test
  void deleteByIdObject() {
    Object id = "0";
    indexedRepositoryDecorator.deleteById(id);
    verify(delegateRepository).deleteById(id);
    verifyZeroInteractions(searchService);
  }

  @Test
  void findOneQuery() {
    indexedRepositoryDecorator.findOne(query);
    verify(delegateRepository).findOne(query);
    verifyZeroInteractions(searchService);
  }

  @Test
  void findOneQueryUnsupported() {
    Entity entity0 = mock(Entity.class);
    when(searchService.searchOne(repositoryEntityType, unsupportedQuery)).thenReturn(entity0);

    indexedRepositoryDecorator.findOne(unsupportedQuery);
    verify(searchService).searchOne(repositoryEntityType, unsupportedQuery);
    verify(delegateRepository).findOneById(any(Object.class), isNull());
  }

  @Test
  void findOneUnknownIndexExceptionRecoverable() {
    Entity entity0 = mock(Entity.class);
    when(searchService.searchOne(repositoryEntityType, unsupportedQuery))
        .thenThrow(new UnknownIndexException("msg"))
        .thenReturn(entity0);
    indexedRepositoryDecorator.findOne(unsupportedQuery);
    verify(searchService, times(2)).searchOne(repositoryEntityType, unsupportedQuery);
    verify(delegateRepository).findOneById(any(Object.class), isNull());
  }

  @Test
  void findOneUnknownIndexExceptionUnrecoverable() {
    when(searchService.searchOne(repositoryEntityType, unsupportedQuery))
        .thenThrow(new UnknownIndexException("index"));
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> indexedRepositoryDecorator.findOne(unsupportedQuery));
    assertThat(exception.getMessage())
        .containsPattern(
            "Error executing query, index for entity type 'My entity type' with id 'entity' does not exist");
  }

  @Test
  void findOneById() {
    Object id = mock(Object.class);
    indexedRepositoryDecorator.findOneById(id);
    verify(delegateRepository).findOneById(id);
    verifyZeroInteractions(searchService);
  }

  @Test
  void findOneByIdFetch() {
    Object id = mock(Object.class);
    Fetch fetch = new Fetch();

    Entity entity = mock(Entity.class);
    when(delegateRepository.findOneById(id, fetch)).thenReturn(entity);
    assertEquals(entity, indexedRepositoryDecorator.findOneById(id, fetch));
    verify(delegateRepository, times(1)).findOneById(id, fetch);
    verifyZeroInteractions(searchService);
  }

  @Test
  void getEntityType() {
    assertEquals(repositoryEntityType, indexedRepositoryDecorator.getEntityType());
  }

  @Test
  void getName() {
    assertEquals(repositoryEntityType.getId(), indexedRepositoryDecorator.getName());
  }

  @Test
  void updateEntity() {
    String id = "id0";
    Entity entity = when(mock(Entity.class).get(idAttrName)).thenReturn(id).getMock();
    indexedRepositoryDecorator.update(entity);
    verify(delegateRepository).update(entity);
    verifyZeroInteractions(searchService);
  }

  @Test
  void updateStream() {
    Stream<Entity> entities = Stream.empty();
    indexedRepositoryDecorator.update(entities);
    verify(delegateRepository, times(1)).update(entities);
    verifyZeroInteractions(searchService);
  }

  @Test
  void findAllStream() {
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    Stream<Object> entityIds = Stream.of(id0, id1);
    when(delegateRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
    Stream<Entity> expectedEntities = indexedRepositoryDecorator.findAll(entityIds);
    assertEquals(asList(entity0, entity1), expectedEntities.collect(toList()));
    verifyZeroInteractions(searchService);
  }

  @Test
  void findAllStreamFetch() {
    Fetch fetch = new Fetch();
    Object id0 = "id0";
    Object id1 = "id1";
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    Stream<Object> entityIds = Stream.of(id0, id1);
    when(delegateRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
    Stream<Entity> expectedEntities = indexedRepositoryDecorator.findAll(entityIds, fetch);
    assertEquals(asList(entity0, entity1), expectedEntities.collect(toList()));
    verifyZeroInteractions(searchService);
  }

  @Test
  void findAllQuery() {
    indexedRepositoryDecorator.findAll(query);
    verify(delegateRepository, times(1)).findAll(query);
    verifyZeroInteractions(searchService);
  }

  @SuppressWarnings("unchecked")
  @Test
  void findAllQueryUnsupported() {
    indexedRepositoryDecorator.findAll(unsupportedQuery);
    verify(searchService).search(repositoryEntityType, unsupportedQuery);
    verify(delegateRepository).findAll(any(Stream.class), isNull());
  }

  @SuppressWarnings("unchecked")
  @Test
  void findAllUnknownIndexExceptionRecoverable() {
    Stream<Object> entityStream = mock(Stream.class);
    when(searchService.search(repositoryEntityType, unsupportedQuery))
        .thenThrow(new UnknownIndexException("index"))
        .thenReturn(entityStream);
    indexedRepositoryDecorator.findAll(unsupportedQuery);
    verify(searchService, times(2)).search(repositoryEntityType, unsupportedQuery);
    verify(delegateRepository).findAll(any(Stream.class), isNull());
  }

  @Test
  void findAllUnknownIndexExceptionUnrecoverable() {
    when(searchService.search(repositoryEntityType, unsupportedQuery))
        .thenThrow(new UnknownIndexException("index"));
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> indexedRepositoryDecorator.findAll(unsupportedQuery));
    assertThat(exception.getMessage())
        .containsPattern(
            "Error executing query, index for entity type 'My entity type' with id 'entity' does not exist");
  }

  @Test
  void forEachBatched() {
    Fetch fetch = new Fetch();
    @SuppressWarnings("unchecked")
    Consumer<List<Entity>> consumer = mock(Consumer.class);
    indexedRepositoryDecorator.forEachBatched(fetch, consumer, 12);
    verify(delegateRepository, times(1)).forEachBatched(fetch, consumer, 12);
  }

  @Test
  void iterator() {
    indexedRepositoryDecorator.iterator();
    verify(delegateRepository, times(1)).iterator();
    verifyZeroInteractions(searchService);
  }

  @Test
  void getCapabilities() {
    assertEquals(
        of(AGGREGATEABLE, QUERYABLE, MANAGABLE, VALIDATE_NOTNULL_CONSTRAINT),
        indexedRepositoryDecorator.getCapabilities());
  }

  @Test
  void getQueryOperators() {
    assertEquals(allOf(Operator.class), indexedRepositoryDecorator.getQueryOperators());
  }

  @Test
  void query() {
    assertEquals(indexedRepositoryDecorator, indexedRepositoryDecorator.query().getRepository());
    verifyZeroInteractions(searchService);
  }

  @Test
  void unsupportedQueryWithComputedAttributes() {
    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    QueryRule qRule1 = mock(QueryRule.class);
    QueryRule qRule2 = mock(QueryRule.class);

    when(qRule1.getField()).thenReturn("attr1");
    when(qRule2.getField()).thenReturn("attr2");
    when(qRule1.getOperator()).thenReturn(EQUALS);
    when(qRule2.getOperator()).thenReturn(OR);
    when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
    when(qRule2.getNestedRules()).thenReturn(Collections.emptyList());
    when(q.getRules()).thenReturn(newArrayList(qRule1, qRule2));

    Attribute attr1 = mock(Attribute.class);
    when(repositoryEntityType.getAttribute("attr1")).thenReturn(attr1);
    when(attr1.hasExpression()).thenReturn(true);

    Attribute attr2 = mock(Attribute.class);
    when(repositoryEntityType.getAttribute("attr2")).thenReturn(attr2);
    when(attr2.hasExpression()).thenReturn(true);

    indexedRepositoryDecorator.count(q);
    verify(searchService).count(repositoryEntityType, q);
    verify(delegateRepository, never()).count(q);
  }

  @Test
  void unsupportedQueryWithSortOnComputedAttributes() {
    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    Sort sort = mock(Sort.class);
    when(q.getSort()).thenReturn(sort);

    Attribute attr1 = mock(Attribute.class);
    when(repositoryEntityType.getAttribute("attr1")).thenReturn(attr1);
    when(attr1.hasExpression()).thenReturn(true);

    Attribute attr2 = mock(Attribute.class);
    when(repositoryEntityType.getAttribute("attr2")).thenReturn(attr2);
    when(attr2.hasExpression()).thenReturn(true);

    Sort.Order o1 = mock(Sort.Order.class);
    Sort.Order o2 = mock(Sort.Order.class);

    when(o1.getAttr()).thenReturn("attr1");
    when(o2.getAttr()).thenReturn("attr2");

    when(sort.spliterator()).thenReturn(newArrayList(o1, o2).spliterator());

    indexedRepositoryDecorator.count(q);
    verify(searchService).count(repositoryEntityType, q);
    verify(delegateRepository, never()).count(q);
  }

  @Test
  void unsupportedQueryWithNestedQueryRuleField() {
    String refAttrName = "refAttr";
    String attrName = "attr";
    String queryRuleField = refAttrName + '.' + attrName;
    Attribute refAttr = mock(Attribute.class);
    EntityType refEntityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(refEntityType.getAttribute(attrName)).thenReturn(attr);
    when(refAttr.getRefEntity()).thenReturn(refEntityType);
    when(repositoryEntityType.getAttribute(refAttrName)).thenReturn(refAttr);
    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn(queryRuleField);
    when(q.getRules()).thenReturn(singletonList(queryRule));
    indexedRepositoryDecorator.count(q);
    verify(searchService).count(repositoryEntityType, q);
    verify(delegateRepository, never()).count(q);
  }
}

package org.molgenis.api.data.v3;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.web.rsql.RSQLValueParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class QueryV3MapperTest extends AbstractMockitoTest {
  @Mock private RSQLValueParser rsqlValueParser;
  private QueryV3Mapper queryMapper;

  @BeforeMethod
  public void setUpBeforeMethod() {
    queryMapper = new QueryV3Mapper(rsqlValueParser);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testQueryV3Mapper() {
    new QueryV3Mapper(null);
  }

  @Test
  public void testMapEquals() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.EQUALS).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute);
    Object parsedValue = mock(Object.class);
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        queryMapper.map(query, repository), new QueryImpl<>(repository).eq("test", parsedValue));
  }

  @Test
  public void testMapEqualsNull() {
    Query query = Query.builder().setItem("test").setOperator(Operator.EQUALS).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute);
    when(rsqlValueParser.parse(null, attribute)).thenReturn(null);

    assertEquals(queryMapper.map(query, repository), new QueryImpl<>(repository).eq("test", null));
  }

  @Test
  public void testMapNotEquals() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.NOT_EQUALS).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute);
    Object parsedValue = mock(Object.class);
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        queryMapper.map(query, repository),
        new QueryImpl<>(repository).not().eq("test", parsedValue));
  }

  @Test
  public void testMapIn() {
    String value0 = "value0";
    String value1 = "value1";
    Query query =
        Query.builder()
            .setItem("test")
            .setOperator(Operator.IN)
            .setValue(asList(value0, value1))
            .build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute);
    Object parsedValue0 = mock(Object.class);
    doReturn(parsedValue0).when(rsqlValueParser).parse(value0, attribute);
    Object parsedValue1 = mock(Object.class);
    doReturn(parsedValue1).when(rsqlValueParser).parse(value1, attribute);

    assertEquals(
        queryMapper.map(query, repository),
        new QueryImpl<>(repository).in("test", asList(parsedValue0, parsedValue1)));
  }

  @Test
  public void testMapNotIn() {
    String value0 = "value0";
    String value1 = "value1";
    Query query =
        Query.builder()
            .setItem("test")
            .setOperator(Operator.NOT_IN)
            .setValue(asList(value0, value1))
            .build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute);
    Object parsedValue0 = mock(Object.class);
    doReturn(parsedValue0).when(rsqlValueParser).parse(value0, attribute);
    Object parsedValue1 = mock(Object.class);
    doReturn(parsedValue1).when(rsqlValueParser).parse(value1, attribute);

    assertEquals(
        queryMapper.map(query, repository),
        new QueryImpl<>(repository).not().in("test", asList(parsedValue0, parsedValue1)));
  }

  @Test
  public void testMapMatches() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.MATCHES).setValue(value).build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(
        queryMapper.map(query, repository), new QueryImpl<>(repository).search("test", value));
  }

  @Test
  public void testMapMatchesAllAttributes() {
    String value = "value";
    Query query = Query.builder().setOperator(Operator.MATCHES).setValue(value).build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(queryMapper.map(query, repository), new QueryImpl<>(repository).search(value));
  }

  @Test
  public void testMapContains() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.CONTAINS).setValue(value).build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(
        queryMapper.map(query, repository), new QueryImpl<>(repository).like("test", value));
  }

  @Test
  public void testMapLessThan() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.LESS_THAN).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute, AttributeType.INT);
    Object parsedValue = "parsedValue";
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        queryMapper.map(query, repository), new QueryImpl<>(repository).lt("test", parsedValue));
  }

  @Test
  public void testMapLessThanOrEqualTo() {
    String value = "value";
    Query query =
        Query.builder()
            .setItem("test")
            .setOperator(Operator.LESS_THAN_OR_EQUAL_TO)
            .setValue(value)
            .build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute, AttributeType.INT);
    Object parsedValue = "parsedValue";
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        queryMapper.map(query, repository), new QueryImpl<>(repository).le("test", parsedValue));
  }

  @Test
  public void testMapGreaterThan() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.GREATER_THAN).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute, AttributeType.INT);
    Object parsedValue = "parsedValue";
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        queryMapper.map(query, repository), new QueryImpl<>(repository).gt("test", parsedValue));
  }

  @Test
  public void testMapGreaterThanOrEqualTo() {
    String value = "value";
    Query query =
        Query.builder()
            .setItem("test")
            .setOperator(Operator.GREATER_THAN_OR_EQUAL_TO)
            .setValue(value)
            .build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute, AttributeType.INT);
    Object parsedValue = "parsedValue";
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        queryMapper.map(query, repository), new QueryImpl<>(repository).ge("test", parsedValue));
  }

  @Test
  public void testMapAnd() {
    String value0 = "value0";
    String value1 = "value1";
    Query query =
        Query.builder()
            .setItem("test")
            .setOperator(Operator.AND)
            .setValue(
                asList(
                    Query.builder().setOperator(Operator.MATCHES).setValue(value0).build(),
                    Query.builder().setOperator(Operator.MATCHES).setValue(value1).build()))
            .build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(
        queryMapper.map(query, repository),
        new QueryImpl<>(repository).nest().search(value0).and().search(value1).unnest());
  }

  @Test
  public void testMapOr() {
    String value0 = "value0";
    String value1 = "value1";
    Query query =
        Query.builder()
            .setItem("test")
            .setOperator(Operator.OR)
            .setValue(
                asList(
                    Query.builder().setOperator(Operator.MATCHES).setValue(value0).build(),
                    Query.builder().setOperator(Operator.MATCHES).setValue(value1).build()))
            .build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(
        queryMapper.map(query, repository),
        new QueryImpl<>(repository).nest().search(value0).or().search(value1).unnest());
  }

  @Test(expectedExceptions = UnknownAttributeException.class)
  public void testMapEqualsUnknownAttribute() {
    String value = "value";
    Query query =
        Query.builder()
            .setItem("unknownAttribute")
            .setOperator(Operator.EQUALS)
            .setValue(value)
            .build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    EntityType entityType = mock(EntityType.class);
    when(repository.getEntityType()).thenReturn(entityType);

    queryMapper.map(query, repository);
  }

  @Test(expectedExceptions = UnexpectedEnumException.class)
  public void testMapCompoundAttribute() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.EQUALS).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute, AttributeType.COMPOUND);

    queryMapper.map(query, repository);
  }

  private Repository<Entity> createMockRepository(Attribute attribute) {
    return createMockRepository(attribute, AttributeType.STRING);
  }

  private Repository<Entity> createMockRepository(
      Attribute attribute, AttributeType attributeType) {
    when(attribute.getDataType()).thenReturn(attributeType);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttribute("test")).thenReturn(attribute);

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);
    when(repository.getEntityType()).thenReturn(entityType);

    return repository;
  }
}

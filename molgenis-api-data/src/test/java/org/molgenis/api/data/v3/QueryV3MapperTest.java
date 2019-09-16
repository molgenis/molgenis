package org.molgenis.api.data.v3;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class QueryV3MapperTest extends AbstractMockitoTest {
  @Mock private RSQLValueParser rsqlValueParser;
  private QueryV3Mapper queryMapper;

  @BeforeEach
  void setUpBeforeMethod() {
    queryMapper = new QueryV3Mapper(rsqlValueParser);
  }

  @Test
  void testQueryV3Mapper() {
    assertThrows(NullPointerException.class, () -> new QueryV3Mapper(null));
  }

  @Test
  void testMapEquals() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.EQUALS).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute);
    Object parsedValue = mock(Object.class);
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        new QueryImpl<>(repository).eq("test", parsedValue), queryMapper.map(query, repository));
  }

  @Test
  void testMapEqualsNull() {
    Query query = Query.builder().setItem("test").setOperator(Operator.EQUALS).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute);
    when(rsqlValueParser.parse(null, attribute)).thenReturn(null);

    assertEquals(new QueryImpl<>(repository).eq("test", null), queryMapper.map(query, repository));
  }

  @Test
  void testMapNotEquals() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.NOT_EQUALS).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute);
    Object parsedValue = mock(Object.class);
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        new QueryImpl<>(repository).not().eq("test", parsedValue),
        queryMapper.map(query, repository));
  }

  @Test
  void testMapIn() {
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
        new QueryImpl<>(repository).in("test", asList(parsedValue0, parsedValue1)),
        queryMapper.map(query, repository));
  }

  @Test
  void testMapNotIn() {
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
        new QueryImpl<>(repository).not().in("test", asList(parsedValue0, parsedValue1)),
        queryMapper.map(query, repository));
  }

  @Test
  void testMapMatches() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.MATCHES).setValue(value).build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(
        new QueryImpl<>(repository).search("test", value), queryMapper.map(query, repository));
  }

  @Test
  void testMapMatchesAllAttributes() {
    String value = "value";
    Query query = Query.builder().setOperator(Operator.MATCHES).setValue(value).build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(new QueryImpl<>(repository).search(value), queryMapper.map(query, repository));
  }

  @Test
  void testMapContains() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.CONTAINS).setValue(value).build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(
        new QueryImpl<>(repository).like("test", value), queryMapper.map(query, repository));
  }

  @Test
  void testMapLessThan() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.LESS_THAN).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute, AttributeType.INT);
    Object parsedValue = "parsedValue";
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        new QueryImpl<>(repository).lt("test", parsedValue), queryMapper.map(query, repository));
  }

  @Test
  void testMapLessThanOrEqualTo() {
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
        new QueryImpl<>(repository).le("test", parsedValue), queryMapper.map(query, repository));
  }

  @Test
  void testMapGreaterThan() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.GREATER_THAN).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute, AttributeType.INT);
    Object parsedValue = "parsedValue";
    when(rsqlValueParser.parse(value, attribute)).thenReturn(parsedValue);

    assertEquals(
        new QueryImpl<>(repository).gt("test", parsedValue), queryMapper.map(query, repository));
  }

  @Test
  void testMapGreaterThanOrEqualTo() {
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
        new QueryImpl<>(repository).ge("test", parsedValue), queryMapper.map(query, repository));
  }

  @Test
  void testMapAnd() {
    String value0 = "value0";
    String value1 = "value1";
    Query query =
        Query.builder()
            .setOperator(Operator.AND)
            .setValue(
                asList(
                    Query.builder().setOperator(Operator.MATCHES).setValue(value0).build(),
                    Query.builder().setOperator(Operator.MATCHES).setValue(value1).build()))
            .build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(
        new QueryImpl<>(repository).nest().search(value0).and().search(value1).unnest(),
        queryMapper.map(query, repository));
  }

  @Test
  void testMapOr() {
    String value0 = "value0";
    String value1 = "value1";
    Query query =
        Query.builder()
            .setOperator(Operator.OR)
            .setValue(
                asList(
                    Query.builder().setOperator(Operator.MATCHES).setValue(value0).build(),
                    Query.builder().setOperator(Operator.MATCHES).setValue(value1).build()))
            .build();

    @SuppressWarnings("unchecked")
    Repository<Entity> repository = mock(Repository.class);

    assertEquals(
        new QueryImpl<>(repository).nest().search(value0).or().search(value1).unnest(),
        queryMapper.map(query, repository));
  }

  @Test
  void testMapEqualsUnknownAttribute() {
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

    assertThrows(UnknownAttributeException.class, () -> queryMapper.map(query, repository));
  }

  @Test
  void testMapCompoundAttribute() {
    String value = "value";
    Query query =
        Query.builder().setItem("test").setOperator(Operator.EQUALS).setValue(value).build();

    Attribute attribute = mock(Attribute.class);
    Repository<Entity> repository = createMockRepository(attribute, AttributeType.COMPOUND);

    assertThrows(UnexpectedEnumException.class, () -> queryMapper.map(query, repository));
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

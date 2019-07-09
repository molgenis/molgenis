package org.molgenis.api.data.v3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.QueryRule.Operator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.UnexpectedEnumException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class QueryV3MapperTest extends AbstractMockitoTest {

  private QueryV3Mapper queryMapper;

  @BeforeMethod
  public void setUpBeforeMethod() {
    queryMapper = new QueryV3Mapper();
  }

  @Test
  public void testMapStringEquals() {
    Query query = new Query();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.eq("test", "value");

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapStringLike() {
    Query query = new Query();
    query.addRule("test", Operator.LIKE, Collections.singletonList("value"));

    EntityType entityType = mock(EntityType.class);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.like("test", "value");

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapStringSearch() {
    Query query = new Query();
    query.addRule("test", Operator.SEARCH, Collections.singletonList("value"));

    EntityType entityType = mock(EntityType.class);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.search("test", "value");

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapStringSearchNoAttr() {
    Query query = new Query();
    query.search(Collections.singletonList("value"));

    EntityType entityType = mock(EntityType.class);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.search("value");

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapRange() {
    Query query = new Query();
    query.addRule("test", Operator.RANGE, Arrays.asList("1.1", "4"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.DECIMAL);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.rng("test", 1.1, 4.0);

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapIn() {
    Query query = new Query();
    query.addRule("test", Operator.IN, Arrays.asList("test", "test2"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.in("test", Arrays.asList("test", "test2"));

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapGt() {
    Query query = new Query();
    query.addRule("test", Operator.GREATER, Collections.singletonList("3"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.INT);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.gt("test", 3);

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapGe() {
    Query query = new Query();
    query.addRule("test", Operator.GREATER_EQUAL, Collections.singletonList("2019-01-01"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.DATE);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.ge("test", parseLocalDate("2019-01-01"));

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapLe() {
    Query query = new Query();
    query.addRule("test", Operator.LESS_EQUAL, Collections.singletonList("3"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.INT);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.le("test", 3);

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapLt() {
    Query query = new Query();
    query.addRule("test", Operator.LESS, Collections.singletonList("2019-01-01"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.DATE);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.lt("test", parseLocalDate("2019-01-01"));

    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapAnd() {
    Query query = new Query();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value"));
    query.and();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value2"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.eq("test", "value");
    expected.and();
    expected.eq("test", "value2");
    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapOr() {
    Query query = new Query();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value"));
    query.or();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value2"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.eq("test", "value");
    expected.or();
    expected.eq("test", "value2");
    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapNot() {
    Query query = new Query();
    query.not();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value2"));

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.not();
    expected.eq("test", "value2");
    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test
  public void testMapNested() {
    Query query = new Query();
    query.nest();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value"));
    query.and();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value2"));
    query.unnest();

    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    when(entityType.getAttribute("test")).thenReturn(attr);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    QueryImpl expected = new QueryImpl<>();
    expected.nest().eq("test", "value").and().eq("test", "value2").unnest();
    assertEquals(queryMapper.map(query, repo), expected);
  }

  @Test(expectedExceptions = UnexpectedEnumException.class)
  public void testMapShould() {
    Query query = new Query();
    query.addRule("test", Operator.SHOULD, Collections.singletonList("value2"));

    EntityType entityType = mock(EntityType.class);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    queryMapper.map(query, repo);
  }

  @Test(expectedExceptions = UnexpectedEnumException.class)
  public void testMapDisMax() {
    Query query = new Query();
    query.addRule("test", Operator.DIS_MAX, Collections.singletonList("value2"));

    EntityType entityType = mock(EntityType.class);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    queryMapper.map(query, repo);
  }

  @Test(expectedExceptions = UnexpectedEnumException.class)
  public void testMapFuzzyMatch() {
    Query query = new Query();
    query.addRule("test", Operator.FUZZY_MATCH, Collections.singletonList("value2"));

    EntityType entityType = mock(EntityType.class);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    queryMapper.map(query, repo);
  }

  @Test(expectedExceptions = UnexpectedEnumException.class)
  public void testMapNGram() {
    Query query = new Query();
    query.addRule("test", Operator.FUZZY_MATCH_NGRAM, Collections.singletonList("value2"));

    EntityType entityType = mock(EntityType.class);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    queryMapper.map(query, repo);
  }

  @Test(expectedExceptions = UnknownAttributeException.class)
  public void testMapUnknownAttr() {
    Query query = new Query();
    query.addRule("test", Operator.EQUALS, Collections.singletonList("value2"));

    EntityType entityType = mock(EntityType.class);
    Repository<Entity> repo = mock(Repository.class);
    when(repo.getEntityType()).thenReturn(entityType);

    queryMapper.map(query, repo);
  }
}

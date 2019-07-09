package org.molgenis.api.data.v3;

import static org.testng.Assert.assertEquals;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import java.util.Collections;
import org.molgenis.api.convert.RsqlConverter;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.QueryRule.Operator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RsqlConverterTest extends AbstractMockitoTest {
  private RsqlConverter rsqlConverter;

  @BeforeMethod
  public void beforeMethod() {
    rsqlConverter = new RsqlConverter(new RSQLParser());
  }

  @Test
  public void testEquals() throws RSQLParserException {
    Query q = rsqlConverter.createQuery("name==piet");
    assertEquals(
        q, new Query().addRule("name", Operator.EQUALS, Collections.singletonList("piet")));

    q = rsqlConverter.createQuery("name=='piet paulusma'");
    assertEquals(
        q,
        new Query().addRule("name", Operator.EQUALS, Collections.singletonList("piet paulusma")));

    q = rsqlConverter.createQuery("age==87");
    assertEquals(q, new Query().addRule("age", Operator.EQUALS, Collections.singletonList("87")));
  }

  @Test
  public void testGreaterThanOrEqual() throws RSQLParserException {
    Query q = rsqlConverter.createQuery("age>=87");
    assertEquals(
        q, new Query().addRule("age", Operator.GREATER_EQUAL, Collections.singletonList("87")));
  }

  @Test
  public void testGreaterThan() throws RSQLParserException {
    Query q = rsqlConverter.createQuery("age>87");
    assertEquals(q, new Query().addRule("age", Operator.GREATER, Collections.singletonList("87")));
  }

  @Test
  public void testLessThanOrEqual() throws RSQLParserException {
    Query q = rsqlConverter.createQuery("age<=87");
    assertEquals(
        q, new Query().addRule("age", Operator.LESS_EQUAL, Collections.singletonList("87")));
  }

  @Test
  public void testLessThan() throws RSQLParserException {
    Query q = rsqlConverter.createQuery("age<87");
    assertEquals(q, new Query().addRule("age", Operator.LESS, Collections.singletonList("87")));
  }

  @Test
  public void testAnd() throws RSQLParserException {
    Query q = rsqlConverter.createQuery("name==piet and age==87");
    assertEquals(
        q,
        new Query()
            .nest()
            .addRule("name", Operator.EQUALS, Collections.singletonList("piet"))
            .and()
            .addRule("age", Operator.EQUALS, Collections.singletonList("87"))
            .unnest());

    q = rsqlConverter.createQuery("name==piet;age==87");
    assertEquals(
        q,
        new Query()
            .nest()
            .addRule("name", Operator.EQUALS, Collections.singletonList("piet"))
            .and()
            .addRule("age", Operator.EQUALS, Collections.singletonList("87"))
            .unnest());
  }

  @Test
  public void testOr() throws RSQLParserException {
    Query q = rsqlConverter.createQuery("name==piet or age==87");
    assertEquals(
        q,
        new Query()
            .nest()
            .addRule("name", Operator.EQUALS, Collections.singletonList("piet"))
            .or()
            .addRule("age", Operator.EQUALS, Collections.singletonList("87"))
            .unnest());

    q = rsqlConverter.createQuery("name==piet,age==87");
    assertEquals(
        q,
        new Query()
            .nest()
            .addRule("name", Operator.EQUALS, Collections.singletonList("piet"))
            .or()
            .addRule("age", Operator.EQUALS, Collections.singletonList("87"))
            .unnest());
  }

  @Test
  public void testComplexQuery() throws RSQLParserException {
    Query q = rsqlConverter.createQuery("((name==piet;age==87),(name==klaas;age>100))");
    assertEquals(
        q,
        new Query()
            .nest()
            .nest()
            .addRule("name", Operator.EQUALS, Collections.singletonList("piet"))
            .and()
            .addRule("age", Operator.EQUALS, Collections.singletonList("87"))
            .unnest()
            .or()
            .nest()
            .addRule("name", Operator.EQUALS, Collections.singletonList("klaas"))
            .and()
            .addRule("age", Operator.GREATER, Collections.singletonList("100"))
            .unnest()
            .unnest());
  }

  @Test
  public void testXrefIntegerIdValue() {
    Query q = rsqlConverter.createQuery("gender==2");
    assertEquals(q, new Query().addRule("gender", Operator.EQUALS, Collections.singletonList("2")));
  }
}

package org.molgenis.data.support;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.molgenis.data.QueryRule.Operator.OR;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;

class QueryImplTest {
  @Test
  void rng() {
    Query<Entity> q = new QueryImpl<>().rng("field", "min", "max");
    QueryRule expectedRule = new QueryRule("field", Operator.RANGE, Arrays.asList("min", "max"));
    assertEquals(asList(expectedRule), q.getRules());
  }

  @Test
  void nest() {
    Query<Entity> q = new QueryImpl<>().nest().eq("field", "value").unnest();
    QueryRule expectedRule =
        new QueryRule(Arrays.asList(new QueryRule("field", Operator.EQUALS, "value")));
    assertEquals(asList(expectedRule), q.getRules());
  }

  @Test
  void nestOr() {
    Query<Entity> q =
        new QueryImpl<>().nest().eq("field", "value1").or().eq("field", "value2").unnest();
    QueryRule expectedRule =
        new QueryRule(
            Arrays.asList(
                new QueryRule("field", Operator.EQUALS, "value1"),
                new QueryRule(Operator.OR),
                new QueryRule("field", Operator.EQUALS, "value2")));
    assertEquals(asList(expectedRule), q.getRules());
  }

  @Test
  void nestAnd() {
    Query<Entity> q =
        new QueryImpl<>().nest().eq("field", "value1").and().eq("field", "value2").unnest();
    QueryRule expectedRule =
        new QueryRule(
            Arrays.asList(
                new QueryRule("field", Operator.EQUALS, "value1"),
                new QueryRule(Operator.AND),
                new QueryRule("field", Operator.EQUALS, "value2")));
    assertEquals(asList(expectedRule), q.getRules());
  }

  @Test
  void nestDeep() {
    // A OR (B AND (C OR D))
    Query<Entity> q =
        new QueryImpl<>()
            .eq("field1", "value1")
            .or()
            .nest()
            .eq("field2", "value2")
            .and()
            .nest()
            .eq("field3", "value3")
            .or()
            .eq("field4", "value4")
            .unnest()
            .unnest();
    QueryRule expectedRule1 = new QueryRule("field1", Operator.EQUALS, "value1");
    QueryRule expectedRule1a = new QueryRule("field2", Operator.EQUALS, "value2");
    QueryRule expectedRule1b1 = new QueryRule("field3", Operator.EQUALS, "value3");
    QueryRule expectedRule1b2 = new QueryRule("field4", Operator.EQUALS, "value4");
    QueryRule expectedRule1b =
        new QueryRule(Arrays.asList(expectedRule1b1, new QueryRule(Operator.OR), expectedRule1b2));
    QueryRule expectedRule2 =
        new QueryRule(Arrays.asList(expectedRule1a, new QueryRule(Operator.AND), expectedRule1b));
    assertEquals(asList(expectedRule1, new QueryRule(OR), expectedRule2), q.getRules());
  }

  @Test
  void setFetch() {
    Fetch fetch = new Fetch();
    QueryImpl<Entity> q = new QueryImpl<>();
    q.setFetch(fetch);
    assertEquals(q.getFetch(), fetch);
  }

  @Test
  void fetch() {
    Fetch fetch = new QueryImpl<>().fetch();
    assertFalse(fetch.iterator().hasNext());
  }

  @Test
  void fetchFetch() {
    Fetch fetch = new Fetch().field("field0");
    assertEquals(new QueryImpl<>().fetch(fetch).getFetch(), fetch);
  }

  @Test
  void equalsFetch() {
    QueryImpl<Entity> q1 = new QueryImpl<>();
    q1.fetch().field("field0");

    QueryImpl<Entity> q2 = new QueryImpl<>();
    q2.fetch().field("field0");
    assertEquals(q2, q1);
  }

  @Test
  void equalsFetchFalse() {
    QueryImpl<Entity> q1 = new QueryImpl<>();
    q1.fetch().field("field0");

    QueryImpl<Entity> q2 = new QueryImpl<>();
    q2.fetch().field("field1");
    assertEquals(q2, q1);
  }

  @Test
  void queryImplQueryFetch() {
    Query<Entity> q1 = new QueryImpl<>();
    q1.fetch().field("field0");

    QueryImpl<Entity> q2 = new QueryImpl<>(q1);
    assertEquals(q2.getFetch(), q1.getFetch());
  }

  @Test
  void equals() {
    QueryImpl<Entity> q1 = new QueryImpl<>();
    {
      QueryRule geRule = new QueryRule("jaar", Operator.GREATER_EQUAL, "1995");
      QueryRule andRule = new QueryRule(Operator.AND);
      QueryRule leRule = new QueryRule("jaar", Operator.LESS_EQUAL, "1995");
      List<QueryRule> subSubNestedRules = Arrays.asList(geRule, andRule, leRule);
      List<QueryRule> subNestedRules = Arrays.asList(new QueryRule(subSubNestedRules));
      List<QueryRule> nestedRules = Arrays.asList(new QueryRule(subNestedRules));
      QueryRule rule = new QueryRule(nestedRules);
      q1.addRule(rule);
    }
    QueryImpl<Entity> q2 = new QueryImpl<>();
    {
      QueryRule geRule = new QueryRule("jaar", Operator.GREATER_EQUAL, "1996");
      QueryRule andRule = new QueryRule(Operator.AND);
      QueryRule leRule = new QueryRule("jaar", Operator.LESS_EQUAL, "1996");
      List<QueryRule> subSubNestedRules = Arrays.asList(geRule, andRule, leRule);
      List<QueryRule> subNestedRules = Arrays.asList(new QueryRule(subSubNestedRules));
      List<QueryRule> nestedRules = Arrays.asList(new QueryRule(subNestedRules));
      QueryRule rule = new QueryRule(nestedRules);
      q2.addRule(rule);
    }
    assertNotEquals(q1, q2);
  }
}

package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class QueryImplTest
{
	@Test
	public void rng()
	{
		Query<Entity> q = new QueryImpl<>().rng("field", "min", "max");
		QueryRule expectedRule = new QueryRule("field", Operator.RANGE, Arrays.asList("min", "max"));
		assertEquals(q.getRules(), Arrays.asList(expectedRule));
	}

	@Test
	public void nest()
	{
		Query<Entity> q = new QueryImpl<>().nest().eq("field", "value").unnest();
		QueryRule expectedRule = new QueryRule(Arrays.asList(new QueryRule("field", Operator.EQUALS, "value")));
		assertEquals(q.getRules(), Arrays.asList(expectedRule));
	}

	@Test
	public void nestOr()
	{
		Query<Entity> q = new QueryImpl<>().nest().eq("field", "value1").or().eq("field", "value2").unnest();
		QueryRule expectedRule = new QueryRule(
				Arrays.asList(new QueryRule("field", Operator.EQUALS, "value1"), new QueryRule(Operator.OR),
						new QueryRule("field", Operator.EQUALS, "value2")));
		assertEquals(q.getRules(), Arrays.asList(expectedRule));
	}

	@Test
	public void nestAnd()
	{
		Query<Entity> q = new QueryImpl<>().nest().eq("field", "value1").and().eq("field", "value2").unnest();
		QueryRule expectedRule = new QueryRule(
				Arrays.asList(new QueryRule("field", Operator.EQUALS, "value1"), new QueryRule(Operator.AND),
						new QueryRule("field", Operator.EQUALS, "value2")));
		assertEquals(q.getRules(), Arrays.asList(expectedRule));
	}

	@Test
	public void nestDeep()
	{
		// A OR (B AND (C OR D))
		Query<Entity> q = new QueryImpl<>().eq("field1", "value1")
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
		QueryRule expectedRule1b = new QueryRule(
				Arrays.asList(expectedRule1b1, new QueryRule(Operator.OR), expectedRule1b2));
		QueryRule expectedRule2 = new QueryRule(
				Arrays.asList(expectedRule1a, new QueryRule(Operator.AND), expectedRule1b));
		assertEquals(q.getRules(), Arrays.asList(expectedRule1, new QueryRule(Operator.OR), expectedRule2));
	}

	@Test
	public void setFetch()
	{
		Fetch fetch = new Fetch();
		QueryImpl<Entity> q = new QueryImpl<>();
		q.setFetch(fetch);
		assertEquals(fetch, q.getFetch());
	}

	@Test
	public void fetch()
	{
		Fetch fetch = new QueryImpl<>().fetch();
		assertFalse(fetch.iterator().hasNext());
	}

	@Test
	public void fetchFetch()
	{
		Fetch fetch = new Fetch().field("field0");
		assertEquals(fetch, new QueryImpl<>().fetch(fetch).getFetch());
	}

	@Test
	public void equalsFetch()
	{
		QueryImpl<Entity> q1 = new QueryImpl<>();
		q1.fetch().field("field0");

		QueryImpl<Entity> q2 = new QueryImpl<>();
		q2.fetch().field("field0");
		assertEquals(q1, q2);
	}

	@Test
	public void equalsFetchFalse()
	{
		QueryImpl<Entity> q1 = new QueryImpl<>();
		q1.fetch().field("field0");

		QueryImpl<Entity> q2 = new QueryImpl<>();
		q2.fetch().field("field1");
		assertEquals(q1, q2);
	}

	@Test
	public void queryImplQueryFetch()
	{
		Query<Entity> q1 = new QueryImpl<>();
		q1.fetch().field("field0");

		QueryImpl<Entity> q2 = new QueryImpl<>(q1);
		assertEquals(q1.getFetch(), q2.getFetch());
	}

	@Test
	public void equals()
	{
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

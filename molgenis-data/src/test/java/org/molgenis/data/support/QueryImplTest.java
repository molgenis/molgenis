package org.molgenis.data.support;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.testng.annotations.Test;

public class QueryImplTest
{

	@Test
	public void nest()
	{
		Query q = new QueryImpl().nest().eq("field", "value").unnest();
		QueryRule expectedRule = new QueryRule(Arrays.asList(new QueryRule("field", Operator.EQUALS, "value")));
		assertEquals(q.getRules(), Arrays.asList(expectedRule));
	}

	@Test
	public void nestOr()
	{
		Query q = new QueryImpl().nest().eq("field", "value1").or().eq("field", "value2").unnest();
		QueryRule expectedRule = new QueryRule(Arrays.asList(new QueryRule("field", Operator.EQUALS, "value1"),
				new QueryRule(Operator.OR), new QueryRule("field", Operator.EQUALS, "value2")));
		assertEquals(q.getRules(), Arrays.asList(expectedRule));
	}

	@Test
	public void nestAnd()
	{
		Query q = new QueryImpl().nest().eq("field", "value1").and().eq("field", "value2").unnest();
		QueryRule expectedRule = new QueryRule(Arrays.asList(new QueryRule("field", Operator.EQUALS, "value1"),
				new QueryRule(Operator.AND), new QueryRule("field", Operator.EQUALS, "value2")));
		assertEquals(q.getRules(), Arrays.asList(expectedRule));
	}

	@Test
	public void nestDeep()
	{
		// A OR (B AND (C OR D))
		Query q = new QueryImpl().eq("field1", "value1").or().nest().eq("field2", "value2").and().nest()
				.eq("field3", "value3").or().eq("field4", "value4").unnest().unnest();
		QueryRule expectedRule1 = new QueryRule("field1", Operator.EQUALS, "value1");
		QueryRule expectedRule1a = new QueryRule("field2", Operator.EQUALS, "value2");
		QueryRule expectedRule1b1 = new QueryRule("field3", Operator.EQUALS, "value3");
		QueryRule expectedRule1b2 = new QueryRule("field4", Operator.EQUALS, "value4");
		QueryRule expectedRule1b = new QueryRule(Arrays.asList(expectedRule1b1, new QueryRule(Operator.OR),
				expectedRule1b2));
		QueryRule expectedRule2 = new QueryRule(Arrays.asList(expectedRule1a, new QueryRule(Operator.AND),
				expectedRule1b));
		assertEquals(q.getRules(), Arrays.asList(expectedRule1, new QueryRule(Operator.OR), expectedRule2));
	}
}

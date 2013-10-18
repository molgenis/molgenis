package org.molgenis.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.QueryRule.Operator;
import org.testng.annotations.Test;

public class QueryRuleTest
{

	@Test
	public void equals()
	{
		QueryRule q1 = new QueryRule();
		QueryRule q2 = new QueryRule();
		assertTrue(q1.equals(q2));

		q1 = new QueryRule("field", Operator.EQUALS, "test");
		q2 = new QueryRule("field", Operator.EQUALS, "test");
		assertTrue(q1.equals(q2));

		q1 = new QueryRule("field1", Operator.EQUALS, "test");
		q2 = new QueryRule("field2", Operator.EQUALS, "test");
		assertFalse(q1.equals(q2));

		q1 = new QueryRule("field", Operator.EQUALS, "test");
		q2 = new QueryRule("field", Operator.LIKE, "test");
		assertFalse(q1.equals(q2));

		q1 = new QueryRule("field", Operator.EQUALS, "test1");
		q2 = new QueryRule("field", Operator.EQUALS, "test2");
		assertFalse(q1.equals(q2));
	}

	@Test
	public void getNestedRules()
	{
		QueryRule q = new QueryRule();
		assertNotNull(q.getNestedRules());
		assertTrue(q.getNestedRules().isEmpty());

		QueryRule nested = new QueryRule("field", Operator.EQUALS, "Test");
		q = new QueryRule(Operator.NOT, new QueryRule(nested));
		assertEquals(q.getNestedRules().size(), 1);
		assertEquals(nested, q.getNestedRules().get(0));

		q = new QueryRule(Operator.IN_SUBQUERY, new QueryRule(nested));
		assertEquals(q.getNestedRules().size(), 1);
		assertEquals(nested, q.getNestedRules().get(0));
	}
}

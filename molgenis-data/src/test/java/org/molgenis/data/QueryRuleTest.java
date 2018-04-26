package org.molgenis.data;

import org.molgenis.data.QueryRule.Operator;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.testng.Assert.*;

public class QueryRuleTest
{
	@Test
	public void equals()
	{
		QueryRule q1 = new QueryRule("field", EQUALS, "test");
		QueryRule q2 = new QueryRule("field", EQUALS, "test");
		assertTrue(q1.equals(q2));
	}

	@Test
	public void equalsWithDifferentOperator()
	{
		QueryRule q1 = new QueryRule("field", EQUALS, "test");
		QueryRule q2 = new QueryRule("field", SEARCH, "test");
		assertFalse(q1.equals(q2));
	}

	@Test
	public void equalsWithDifferentValues()
	{
		QueryRule q1 = new QueryRule("field", EQUALS, "test1");
		QueryRule q2 = new QueryRule("field", EQUALS, "test2");
		assertFalse(q1.equals(q2));
	}

	@Test
	public void equalsWithDifferentFields()
	{

		QueryRule q1 = new QueryRule("field1", EQUALS, "test");
		QueryRule q2 = new QueryRule("field2", EQUALS, "test");
		assertFalse(q1.equals(q2));
	}

	@Test
	public void valuesForRangeOperator()
	{
		QueryRule qr = new QueryRule("field", RANGE, Arrays.asList(10, 10));
		assertEquals(qr.getValue(), Arrays.asList(10, 10));
	}

	@Test
	public void equalsWithNestedRules()
	{
		QueryRule q = new QueryRule();
		assertNotNull(q.getNestedRules());
		assertTrue(q.getNestedRules().isEmpty());

		QueryRule nested = new QueryRule("field", EQUALS, "Test");
		q = new QueryRule(Operator.NOT, new QueryRule(nested));
		assertEquals(q.getNestedRules().size(), 1);
		assertEquals(nested, q.getNestedRules().get(0));
	}

	@Test
	public void equalsEntityId()
	{
		Entity valueEntity = mock(Entity.class);
		when(valueEntity.getIdValue()).thenReturn("1");
		String valueId = "1";

		QueryRule q1 = new QueryRule("field", EQUALS, valueEntity);
		QueryRule q2 = new QueryRule("field", EQUALS, valueId);
		assertEquals(q1, q2);
	}

	@Test
	public void equalsInEntity()
	{
		Entity entity1 = mock(Entity.class);
		Entity entity2 = mock(Entity.class);

		when(entity1.getIdValue()).thenReturn("1");
		when(entity2.getIdValue()).thenReturn("2");

		List<Entity> entities = newArrayList(entity1, entity2, entity1);
		List<String> ids = newArrayList("1", "2", "1");

		QueryRule q1 = new QueryRule("field", IN, entities);
		QueryRule q2 = new QueryRule("field", IN, ids);

		assertEquals(q1, q2);
	}
}

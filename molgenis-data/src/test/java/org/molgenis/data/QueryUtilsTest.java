package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.DIS_MAX;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH;
import static org.molgenis.data.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.data.QueryRule.Operator.IN;
import static org.molgenis.data.QueryRule.Operator.NESTED;
import static org.molgenis.data.QueryRule.Operator.OR;
import static org.molgenis.data.QueryRule.Operator.SHOULD;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class QueryUtilsTest
{
	private Query<Entity> query;
	private QueryRule rule3;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod()
	{
		query = mock(Query.class);

		QueryRule rule1 = mock(QueryRule.class);
		when(rule1.getOperator()).thenReturn(IN);
		when(rule1.getNestedRules()).thenReturn(Collections.emptyList());

		QueryRule rule2 = mock(QueryRule.class);
		when(rule2.getOperator()).thenReturn(DIS_MAX);
		when(rule2.getNestedRules()).thenReturn(Collections.emptyList());

		rule3 = mock(QueryRule.class);
		when(rule3.getOperator()).thenReturn(NESTED);
		when(rule3.getNestedRules()).thenReturn(Collections.emptyList());

		List<QueryRule> queryRules = Lists.newArrayList(rule1, rule2, rule3);
		when(query.getRules()).thenReturn(queryRules);
	}

	@Test
	public void containsAnyOperator()
	{
		assertTrue(QueryUtils.containsAnyOperator(query, EnumSet.of(DIS_MAX, FUZZY_MATCH)));
	}

	@Test
	public void notContainsAnyOperator()
	{
		assertFalse(QueryUtils.containsAnyOperator(query, EnumSet.of(FUZZY_MATCH, GREATER_EQUAL, SHOULD)));
	}

	@Test
	public void containsOperator()
	{
		assertTrue(QueryUtils.containsOperator(query, DIS_MAX));
	}

	@Test
	public void notContainsOperator()
	{
		assertFalse(QueryUtils.containsOperator(query, FUZZY_MATCH));
	}

	@Test
	public void emptyQuery()
	{
		assertFalse(QueryUtils.containsAnyOperator(new QueryImpl<>(), EnumSet.allOf(QueryRule.Operator.class)));
	}

	@Test
	public void nestedQueryRules()
	{
		QueryRule nestedRule = mock(QueryRule.class);
		when(nestedRule.getOperator()).thenReturn(EQUALS);
		when(rule3.getNestedRules()).thenReturn(Lists.newArrayList(nestedRule));

		assertTrue(QueryUtils.containsOperator(query, EQUALS));
	}

	@Test
	public void notContainsComputedAttributes()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);

		@SuppressWarnings("unchecked") Query<Entity> q = mock(Query.class);
		QueryRule qRule1 = mock(QueryRule.class);
		QueryRule qRule2 = mock(QueryRule.class);

		when(qRule1.getField()).thenReturn("attr1");
		when(qRule2.getField()).thenReturn("attr2");
		when(qRule1.getOperator()).thenReturn(EQUALS);
		when(qRule2.getOperator()).thenReturn(OR);
		when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
		when(qRule2.getNestedRules()).thenReturn(Collections.emptyList());
		when(q.getRules()).thenReturn(Lists.newArrayList(qRule1, qRule2));

		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		when(entityMetaData.getAttribute("attr1")).thenReturn(attr1);
		when(attr1.getExpression()).thenReturn(null);

		AttributeMetaData attr2 = mock(AttributeMetaData.class);
		when(entityMetaData.getAttribute("attr2")).thenReturn(attr2);
		when(attr2.getExpression()).thenReturn(null);

		assertFalse(QueryUtils.containsComputedAttribute(q.getRules(), entityMetaData));
	}

	@Test
	public void containsComputedAttribute()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);

		@SuppressWarnings("unchecked") Query<Entity> q = mock(Query.class);
		QueryRule qRule1 = mock(QueryRule.class);
		QueryRule qRule2 = mock(QueryRule.class);

		when(qRule1.getField()).thenReturn("attr1");
		when(qRule2.getField()).thenReturn("attr2");
		when(qRule1.getOperator()).thenReturn(EQUALS);
		when(qRule2.getOperator()).thenReturn(OR);
		when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
		when(qRule2.getNestedRules()).thenReturn(Collections.emptyList());
		when(q.getRules()).thenReturn(Lists.newArrayList(qRule1, qRule2));

		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		when(entityMetaData.getAttribute("attr1")).thenReturn(attr1);
		when(attr1.getExpression()).thenReturn(null);

		AttributeMetaData attr2 = mock(AttributeMetaData.class);
		when(entityMetaData.getAttribute("attr2")).thenReturn(attr2);
		when(attr2.getExpression()).thenReturn("${value}");

		assertTrue(QueryUtils.containsComputedAttribute(q.getRules(), entityMetaData));
	}

	@Test
	public void containsNestedComputedAttributes()
	{
		EntityMetaData entityMetaData = mock(EntityMetaData.class);

		@SuppressWarnings("unchecked") Query<Entity> q = mock(Query.class);
		QueryRule qRule1 = mock(QueryRule.class);
		QueryRule qRule2 = mock(QueryRule.class);
		QueryRule nestedRule1 = mock(QueryRule.class);
		QueryRule nestedRule2 = mock(QueryRule.class);

		when(qRule1.getField()).thenReturn("attr1");
		when(qRule2.getField()).thenReturn("attr2");
		when(nestedRule1.getField()).thenReturn("attr3");
		when(nestedRule2.getField()).thenReturn("attr4");

		when(qRule1.getOperator()).thenReturn(EQUALS);
		when(qRule2.getOperator()).thenReturn(OR);
		when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
		when(qRule2.getNestedRules()).thenReturn(Lists.newArrayList(nestedRule1, nestedRule2));
		when(q.getRules()).thenReturn(Lists.newArrayList(qRule1, qRule2));

		AttributeMetaData attr1 = mock(AttributeMetaData.class);
		when(entityMetaData.getAttribute("attr1")).thenReturn(attr1);
		when(attr1.getExpression()).thenReturn(null);

		AttributeMetaData attr2 = mock(AttributeMetaData.class);
		when(entityMetaData.getAttribute("attr2")).thenReturn(attr2);
		when(attr2.getExpression()).thenReturn(null);

		AttributeMetaData attr3 = mock(AttributeMetaData.class);
		when(entityMetaData.getAttribute("attr3")).thenReturn(attr3);
		when(attr1.getExpression()).thenReturn(null);

		AttributeMetaData attr4 = mock(AttributeMetaData.class);
		when(entityMetaData.getAttribute("attr4")).thenReturn(attr4);
		when(attr1.getExpression()).thenReturn("${value}");

		assertTrue(QueryUtils.containsComputedAttribute(q.getRules(), entityMetaData));
	}

}

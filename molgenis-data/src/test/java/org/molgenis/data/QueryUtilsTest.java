package org.molgenis.data;

import com.google.common.collect.Lists;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.*;
import static org.testng.Assert.*;

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
		EntityType entityType = mock(EntityType.class);

		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		QueryRule qRule1 = mock(QueryRule.class);
		QueryRule qRule2 = mock(QueryRule.class);

		when(qRule1.getField()).thenReturn("attr1");
		when(qRule2.getField()).thenReturn("attr2");
		when(qRule1.getOperator()).thenReturn(EQUALS);
		when(qRule2.getOperator()).thenReturn(OR);
		when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
		when(qRule2.getNestedRules()).thenReturn(Collections.emptyList());
		when(q.getRules()).thenReturn(Lists.newArrayList(qRule1, qRule2));

		Attribute attr1 = mock(Attribute.class);
		when(entityType.getAttribute("attr1")).thenReturn(attr1);
		when(attr1.getExpression()).thenReturn(null);

		Attribute attr2 = mock(Attribute.class);
		when(entityType.getAttribute("attr2")).thenReturn(attr2);
		when(attr2.getExpression()).thenReturn(null);

		assertFalse(QueryUtils.containsComputedAttribute(q.getRules(), entityType));
	}

	@Test
	public void containsComputedAttribute()
	{
		EntityType entityType = mock(EntityType.class);

		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		QueryRule qRule1 = mock(QueryRule.class);
		QueryRule qRule2 = mock(QueryRule.class);

		when(qRule1.getField()).thenReturn("attr1");
		when(qRule2.getField()).thenReturn("attr2");
		when(qRule1.getOperator()).thenReturn(EQUALS);
		when(qRule2.getOperator()).thenReturn(OR);
		when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
		when(qRule2.getNestedRules()).thenReturn(Collections.emptyList());
		when(q.getRules()).thenReturn(Lists.newArrayList(qRule1, qRule2));

		Attribute attr1 = mock(Attribute.class);
		when(entityType.getAttribute("attr1")).thenReturn(attr1);
		when(attr1.hasExpression()).thenReturn(false);

		Attribute attr2 = mock(Attribute.class);
		when(entityType.getAttribute("attr2")).thenReturn(attr2);
		when(attr2.hasExpression()).thenReturn(true);

		assertTrue(QueryUtils.containsComputedAttribute(q.getRules(), entityType));
	}

	@Test
	public void containsComputedAttributeNested()
	{
		String refAttrName = "refAttr";
		String attrName = "attr";
		String queryRuleField = refAttrName + '.' + attrName;
		QueryRule queryRule = mock(QueryRule.class);
		when(queryRule.getField()).thenReturn(queryRuleField);
		EntityType entityType = mock(EntityType.class);
		Attribute refAttr = mock(Attribute.class);
		EntityType refEntity = mock(EntityType.class);
		Attribute attr = mock(Attribute.class);
		when(attr.hasExpression()).thenReturn(true);
		when(refEntity.getAttribute(attrName)).thenReturn(attr);
		when(refAttr.getRefEntity()).thenReturn(refEntity);
		when(entityType.getAttribute(refAttrName)).thenReturn(refAttr);
		assertTrue(QueryUtils.containsComputedAttribute(singletonList(queryRule), entityType));
	}

	@Test
	public void containsNestedComputedAttributes()
	{
		EntityType entityType = mock(EntityType.class);

		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
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

		Attribute attr1 = mock(Attribute.class);
		when(entityType.getAttribute("attr1")).thenReturn(attr1);
		when(attr1.hasExpression()).thenReturn(false);

		Attribute attr2 = mock(Attribute.class);
		when(entityType.getAttribute("attr2")).thenReturn(attr2);
		when(attr2.hasExpression()).thenReturn(false);

		Attribute attr3 = mock(Attribute.class);
		when(entityType.getAttribute("attr3")).thenReturn(attr3);
		when(attr1.hasExpression()).thenReturn(false);

		Attribute attr4 = mock(Attribute.class);
		when(entityType.getAttribute("attr4")).thenReturn(attr4);
		when(attr1.hasExpression()).thenReturn(true);

		assertTrue(QueryUtils.containsComputedAttribute(q.getRules(), entityType));
	}

	@Test
	public void testGetQueryRuleAttribute() throws Exception
	{
		String attrName = "attr";
		QueryRule queryRule = mock(QueryRule.class);
		when(queryRule.getField()).thenReturn(attrName);
		EntityType entityType = mock(EntityType.class);
		Attribute attr = mock(Attribute.class);
		when(entityType.getAttribute(attrName)).thenReturn(attr);
		assertEquals(QueryUtils.getQueryRuleAttribute(queryRule, entityType), attr);
	}

	@Test(expectedExceptions = UnknownAttributeException.class)
	public void testGetQueryRuleAttributeUnknown() throws Exception
	{
		String attrName = "unknownAttr";
		QueryRule queryRule = mock(QueryRule.class);
		when(queryRule.getField()).thenReturn(attrName);
		EntityType entityType = mock(EntityType.class);
		when(entityType.getAttribute(attrName)).thenReturn(null);
		QueryUtils.getQueryRuleAttribute(queryRule, entityType);
	}

	@Test
	public void testGetQueryRuleAttributeNested() throws Exception
	{
		String refAttrName = "refAttr";
		String attrName = "attr";
		String queryRuleField = refAttrName + '.' + attrName;
		QueryRule queryRule = mock(QueryRule.class);
		when(queryRule.getField()).thenReturn(queryRuleField);
		EntityType entityType = mock(EntityType.class);
		Attribute refAttr = mock(Attribute.class);
		EntityType refEntity = mock(EntityType.class);
		Attribute attr = mock(Attribute.class);
		when(refEntity.getAttribute(attrName)).thenReturn(attr);
		when(refAttr.getRefEntity()).thenReturn(refEntity);
		when(entityType.getAttribute(refAttrName)).thenReturn(refAttr);
		assertEquals(QueryUtils.getQueryRuleAttribute(queryRule, entityType), attr);
	}

	@Test(expectedExceptions = UnknownAttributeException.class)
	public void testGetQueryRuleAttributeNestedUnknown() throws Exception
	{
		String refAttrName = "refAttr";
		String attrName = "unknownAttr";
		String queryRuleField = refAttrName + '.' + attrName;
		QueryRule queryRule = mock(QueryRule.class);
		when(queryRule.getField()).thenReturn(queryRuleField);
		EntityType entityType = mock(EntityType.class);
		Attribute refAttr = mock(Attribute.class);
		EntityType refEntity = mock(EntityType.class);
		when(refEntity.getAttribute(attrName)).thenReturn(null);
		when(refAttr.getRefEntity()).thenReturn(refEntity);
		when(entityType.getAttribute(refAttrName)).thenReturn(refAttr);
		QueryUtils.getQueryRuleAttribute(queryRule, entityType);
	}

	@Test
	public void containsNestedQueryRuleFieldTrue()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		QueryRule nestedQueryRule0 = mock(QueryRule.class);
		when(nestedQueryRule0.getField()).thenReturn("refAttr.attr");
		QueryRule queryRule0 = mock(QueryRule.class);
		when(queryRule0.getField()).thenReturn("attr");
		QueryRule queryRule1 = mock(QueryRule.class);
		when(queryRule1.getNestedRules()).thenReturn(singletonList(nestedQueryRule0));
		when(q.getRules()).thenReturn(asList(queryRule0, queryRule1));
		assertTrue(QueryUtils.containsNestedQueryRuleField(q));
	}

	@Test
	public void containsNestedQueryRuleFieldFalse()
	{
		@SuppressWarnings("unchecked")
		Query<Entity> q = mock(Query.class);
		QueryRule nestedQueryRule0 = mock(QueryRule.class);
		when(nestedQueryRule0.getField()).thenReturn("refAttr");
		QueryRule queryRule0 = mock(QueryRule.class);
		when(queryRule0.getField()).thenReturn("attr");
		QueryRule queryRule1 = mock(QueryRule.class);
		when(queryRule1.getNestedRules()).thenReturn(singletonList(nestedQueryRule0));
		when(q.getRules()).thenReturn(asList(queryRule0, queryRule1));
		assertFalse(QueryUtils.containsNestedQueryRuleField(q));
	}
}

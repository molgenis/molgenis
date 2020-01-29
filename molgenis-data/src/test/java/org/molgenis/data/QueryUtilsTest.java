package org.molgenis.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.QueryRule.Operator.DIS_MAX;
import static org.molgenis.data.QueryRule.Operator.EQUALS;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH;
import static org.molgenis.data.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.data.QueryRule.Operator.IN;
import static org.molgenis.data.QueryRule.Operator.NESTED;
import static org.molgenis.data.QueryRule.Operator.SHOULD;
import static org.molgenis.data.QueryUtils.getQueryRuleAttribute;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;

class QueryUtilsTest extends AbstractMockitoTest {
  @Mock private Query<Entity> query;

  @Test
  void containsAnyOperator() {
    QueryRule rule1 = mock(QueryRule.class);
    when(rule1.getOperator()).thenReturn(IN);
    when(rule1.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule2 = mock(QueryRule.class);
    when(rule2.getOperator()).thenReturn(DIS_MAX);
    when(rule2.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule3 = mock(QueryRule.class);

    List<QueryRule> queryRules = Lists.newArrayList(rule1, rule2, rule3);
    when(query.getRules()).thenReturn(queryRules);

    assertTrue(QueryUtils.containsAnyOperator(query, EnumSet.of(DIS_MAX, FUZZY_MATCH)));
  }

  @Test
  void notContainsAnyOperator() {
    QueryRule rule1 = mock(QueryRule.class);
    when(rule1.getOperator()).thenReturn(IN);
    when(rule1.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule2 = mock(QueryRule.class);
    when(rule2.getOperator()).thenReturn(DIS_MAX);
    when(rule2.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule3 = mock(QueryRule.class);
    when(rule3.getOperator()).thenReturn(NESTED);
    when(rule3.getNestedRules()).thenReturn(Collections.emptyList());

    List<QueryRule> queryRules = Lists.newArrayList(rule1, rule2, rule3);
    when(query.getRules()).thenReturn(queryRules);

    assertFalse(
        QueryUtils.containsAnyOperator(query, EnumSet.of(FUZZY_MATCH, GREATER_EQUAL, SHOULD)));
  }

  @Test
  void containsOperator() {
    QueryRule rule1 = mock(QueryRule.class);
    when(rule1.getOperator()).thenReturn(IN);
    when(rule1.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule2 = mock(QueryRule.class);
    when(rule2.getOperator()).thenReturn(DIS_MAX);
    when(rule2.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule3 = mock(QueryRule.class);

    List<QueryRule> queryRules = Lists.newArrayList(rule1, rule2, rule3);
    when(query.getRules()).thenReturn(queryRules);

    assertTrue(QueryUtils.containsOperator(query, DIS_MAX));
  }

  @Test
  void notContainsOperator() {
    QueryRule rule1 = mock(QueryRule.class);
    when(rule1.getOperator()).thenReturn(IN);
    when(rule1.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule2 = mock(QueryRule.class);
    when(rule2.getOperator()).thenReturn(DIS_MAX);
    when(rule2.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule3 = mock(QueryRule.class);
    when(rule3.getOperator()).thenReturn(NESTED);
    when(rule3.getNestedRules()).thenReturn(Collections.emptyList());

    List<QueryRule> queryRules = Lists.newArrayList(rule1, rule2, rule3);
    when(query.getRules()).thenReturn(queryRules);

    assertFalse(QueryUtils.containsOperator(query, FUZZY_MATCH));
  }

  @Test
  void emptyQuery() {
    assertFalse(
        QueryUtils.containsAnyOperator(new QueryImpl<>(), EnumSet.allOf(QueryRule.Operator.class)));
  }

  @Test
  void nestedQueryRules() {
    QueryRule rule1 = mock(QueryRule.class);
    when(rule1.getOperator()).thenReturn(IN);
    when(rule1.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule2 = mock(QueryRule.class);
    when(rule2.getOperator()).thenReturn(DIS_MAX);
    when(rule2.getNestedRules()).thenReturn(Collections.emptyList());

    QueryRule rule3 = mock(QueryRule.class);
    when(rule3.getNestedRules()).thenReturn(Collections.emptyList());

    List<QueryRule> queryRules = Lists.newArrayList(rule1, rule2, rule3);
    when(query.getRules()).thenReturn(queryRules);

    QueryRule nestedRule = mock(QueryRule.class);
    when(nestedRule.getOperator()).thenReturn(EQUALS);
    when(rule3.getNestedRules()).thenReturn(Lists.newArrayList(nestedRule));

    assertTrue(QueryUtils.containsOperator(query, EQUALS));
  }

  @Test
  void notContainsComputedAttributes() {
    EntityType entityType = mock(EntityType.class);

    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    QueryRule qRule1 = mock(QueryRule.class);
    QueryRule qRule2 = mock(QueryRule.class);

    when(qRule1.getField()).thenReturn("attr1");
    when(qRule2.getField()).thenReturn("attr2");
    when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
    when(qRule2.getNestedRules()).thenReturn(Collections.emptyList());
    when(q.getRules()).thenReturn(Lists.newArrayList(qRule1, qRule2));

    Attribute attr1 = mock(Attribute.class);
    when(entityType.getAttribute("attr1")).thenReturn(attr1);

    Attribute attr2 = mock(Attribute.class);
    when(entityType.getAttribute("attr2")).thenReturn(attr2);

    assertFalse(QueryUtils.containsComputedAttribute(q.getRules(), entityType));
  }

  @Test
  void containsComputedAttribute() {
    EntityType entityType = mock(EntityType.class);

    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    QueryRule qRule1 = mock(QueryRule.class);
    QueryRule qRule2 = mock(QueryRule.class);

    when(qRule1.getField()).thenReturn("attr1");
    when(qRule2.getField()).thenReturn("attr2");
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
  void containsComputedAttributeNested() {
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
  void containsNestedComputedAttributes() {
    EntityType entityType = mock(EntityType.class);

    @SuppressWarnings("unchecked")
    Query<Entity> q = mock(Query.class);
    QueryRule qRule1 = mock(QueryRule.class);
    QueryRule qRule2 = mock(QueryRule.class);

    when(qRule1.getField()).thenReturn("attr1");

    when(qRule1.getNestedRules()).thenReturn(Collections.emptyList());
    when(q.getRules()).thenReturn(Lists.newArrayList(qRule1, qRule2));

    Attribute attr1 = mock(Attribute.class);
    when(entityType.getAttribute("attr1")).thenReturn(attr1);
    when(attr1.hasExpression()).thenReturn(true);

    assertTrue(QueryUtils.containsComputedAttribute(q.getRules(), entityType));
  }

  @Test
  void testGetQueryRuleAttribute() {
    String attrName = "attr";
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn(attrName);
    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(entityType.getAttribute(attrName)).thenReturn(attr);
    assertEquals(attr, getQueryRuleAttribute(queryRule, entityType));
  }

  @Test
  void testGetQueryRuleAttributeUnknown() {
    String attrName = "unknownAttr";
    QueryRule queryRule = mock(QueryRule.class);
    when(queryRule.getField()).thenReturn(attrName);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttribute(attrName)).thenReturn(null);
    assertThrows(
        UnknownAttributeException.class,
        () -> QueryUtils.getQueryRuleAttribute(queryRule, entityType));
  }

  @Test
  void testGetQueryRuleAttributeNested() {
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
    assertEquals(attr, getQueryRuleAttribute(queryRule, entityType));
  }

  @Test
  void testGetQueryRuleAttributeNestedUnknown() {
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
    assertThrows(
        UnknownAttributeException.class,
        () -> QueryUtils.getQueryRuleAttribute(queryRule, entityType));
  }

  @Test
  void containsNestedQueryRuleFieldTrue() {
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
  void containsNestedQueryRuleFieldFalse() {
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

  @Test
  void testGetAttributePathOneElement() {
    String attributePath = "grandparent";
    EntityType entityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(entityType.getAttributeByName(attributePath)).thenReturn(attribute);
    assertEquals(
        ImmutableList.of(attribute), QueryUtils.getAttributePath(attributePath, entityType));
  }

  @Test
  void testGetAttributePathMultipleElements() {
    EntityType grandparentEntityType = mock(EntityType.class);
    EntityType parentEntityType = mock(EntityType.class);
    EntityType childEntityType = mock(EntityType.class);
    Attribute grandparentAttribute = mock(Attribute.class);
    when(grandparentAttribute.hasRefEntity()).thenReturn(true);
    when(grandparentAttribute.getRefEntity()).thenReturn(parentEntityType);
    Attribute parentAttribute = mock(Attribute.class);
    when(parentAttribute.hasRefEntity()).thenReturn(true);
    when(parentAttribute.getRefEntity()).thenReturn(childEntityType);
    Attribute childAttribute = mock(Attribute.class);
    when(grandparentEntityType.getAttributeByName("grandparent")).thenReturn(grandparentAttribute);
    when(parentEntityType.getAttributeByName("parent")).thenReturn(parentAttribute);
    when(childEntityType.getAttributeByName("child")).thenReturn(childAttribute);
    assertEquals(
        ImmutableList.of(grandparentAttribute, parentAttribute, childAttribute),
        QueryUtils.getAttributePath("grandparent.parent.child", grandparentEntityType));
  }

  @SuppressWarnings("deprecation")
  @Test
  void testGetAttributePathMultipleElementsIllegalAttributeType() {
    EntityType childEntityType = mock(EntityType.class);
    Attribute childAttribute = mock(Attribute.class);
    when(childEntityType.getAttributeByName("child")).thenReturn(childAttribute);
    assertThrows(
        MolgenisQueryException.class,
        () -> QueryUtils.getAttributePath("child.parent", childEntityType));
  }

  @Test
  void testGetAttributePathExpandedExpansionWithIdAttribute() {
    EntityType grandparentEntityType = mock(EntityType.class);
    EntityType parentEntityType = mock(EntityType.class);
    EntityType childEntityType = mock(EntityType.class);
    Attribute grandparentAttribute = mock(Attribute.class);
    when(grandparentAttribute.hasRefEntity()).thenReturn(true);
    when(grandparentAttribute.getRefEntity()).thenReturn(parentEntityType);
    Attribute parentAttribute = mock(Attribute.class);
    when(parentAttribute.hasRefEntity()).thenReturn(true);
    when(parentAttribute.getRefEntity()).thenReturn(childEntityType);
    Attribute childAttribute = mock(Attribute.class);
    when(grandparentEntityType.getAttributeByName("grandparent")).thenReturn(grandparentAttribute);
    when(parentEntityType.getAttributeByName("parent")).thenReturn(parentAttribute);
    when(childEntityType.getIdAttribute()).thenReturn(childAttribute);
    assertEquals(
        ImmutableList.of(grandparentAttribute, parentAttribute, childAttribute),
        QueryUtils.getAttributePathExpanded("grandparent.parent", grandparentEntityType));
  }

  @Test
  void testGetAttributePathExpandedExpansionWithLabelAttribute() {
    EntityType grandparentEntityType = mock(EntityType.class);
    EntityType parentEntityType = mock(EntityType.class);
    EntityType childEntityType = mock(EntityType.class);
    Attribute grandparentAttribute = mock(Attribute.class);
    when(grandparentAttribute.hasRefEntity()).thenReturn(true);
    when(grandparentAttribute.getRefEntity()).thenReturn(parentEntityType);
    Attribute parentAttribute = mock(Attribute.class);
    when(parentAttribute.hasRefEntity()).thenReturn(true);
    when(parentAttribute.getRefEntity()).thenReturn(childEntityType);
    Attribute childAttribute = mock(Attribute.class);
    when(grandparentEntityType.getAttributeByName("grandparent")).thenReturn(grandparentAttribute);
    when(parentEntityType.getAttributeByName("parent")).thenReturn(parentAttribute);
    when(childEntityType.getLabelAttribute()).thenReturn(childAttribute);
    assertEquals(
        ImmutableList.of(grandparentAttribute, parentAttribute, childAttribute),
        QueryUtils.getAttributePathExpanded("grandparent.parent", grandparentEntityType, true));
  }

  @Test
  void testGetAttributePathExpandedNoExpansion() {
    EntityType grandparentEntityType = mock(EntityType.class);
    EntityType parentEntityType = mock(EntityType.class);
    EntityType childEntityType = mock(EntityType.class);
    Attribute grandparentAttribute = mock(Attribute.class);
    when(grandparentAttribute.hasRefEntity()).thenReturn(true);
    when(grandparentAttribute.getRefEntity()).thenReturn(parentEntityType);
    Attribute parentAttribute = mock(Attribute.class);
    when(parentAttribute.hasRefEntity()).thenReturn(true);
    when(parentAttribute.getRefEntity()).thenReturn(childEntityType);
    Attribute childAttribute = mock(Attribute.class);
    when(grandparentEntityType.getAttributeByName("grandparent")).thenReturn(grandparentAttribute);
    when(parentEntityType.getAttributeByName("parent")).thenReturn(parentAttribute);
    when(childEntityType.getAttributeByName("child")).thenReturn(childAttribute);
    assertEquals(
        ImmutableList.of(grandparentAttribute, parentAttribute, childAttribute),
        QueryUtils.getAttributePathExpanded("grandparent.parent.child", grandparentEntityType));
  }
}

package org.molgenis.data;

import static java.lang.String.format;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Streams;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

public class QueryUtils {
  private static final char NESTED_ATTRIBUTE_SEPARATOR = '.';

  private QueryUtils() {}

  public static boolean containsOperator(Query<Entity> q, Operator operator) {
    return containsAnyOperator(q, EnumSet.of(operator));
  }

  public static boolean containsAnyOperator(Query<Entity> q, Set<Operator> operators) {
    return containsAnyOperator(q.getRules(), operators);
  }

  public static boolean containsAnyOperator(List<QueryRule> rules, Set<Operator> operators) {
    for (QueryRule rule : rules) {
      if (!rule.getNestedRules().isEmpty()
          && containsAnyOperator(rule.getNestedRules(), operators)) {
        return true;
      }

      if (operators.contains(rule.getOperator())) {
        return true;
      }
    }

    return false;
  }

  public static boolean containsComputedAttribute(Query<Entity> query, EntityType entityType) {
    return (containsComputedAttribute(query.getSort(), entityType)
        || containsComputedAttribute(query.getRules(), entityType));
  }

  public static boolean containsComputedAttribute(Sort sort, EntityType entityType) {
    return ((sort != null)
        && Streams.stream(sort)
            .anyMatch(order -> entityType.getAttribute(order.getAttr()).hasExpression()));
  }

  public static boolean containsComputedAttribute(
      Iterable<QueryRule> rules, EntityType entityType) {
    for (QueryRule rule : rules) {
      List<QueryRule> nestedRules = rule.getNestedRules();
      if (!nestedRules.isEmpty() && containsComputedAttribute(nestedRules, entityType)) {
        return true;
      }
      Attribute attribute = getQueryRuleAttribute(rule, entityType);
      if (attribute != null && attribute.hasExpression()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns {@code true} if a given query contains any query rule with a nested attribute field
   * (e.g. refAttr.attr).
   *
   * @param q query
   * @return {@code true} if a given query contains any query rule with a nested attribute field
   */
  public static boolean containsNestedQueryRuleField(Query<Entity> q) {
    return containsNestedQueryRuleFieldRec(q.getRules());
  }

  private static boolean containsNestedQueryRuleFieldRec(List<QueryRule> rules) {
    for (QueryRule rule : rules) {
      String queryRuleField = rule.getField();
      if (queryRuleField != null && queryRuleField.indexOf(NESTED_ATTRIBUTE_SEPARATOR) != -1) {
        return true;
      }

      List<QueryRule> nestedRules = rule.getNestedRules();
      if (nestedRules != null && containsNestedQueryRuleFieldRec(nestedRules)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the attribute for a query rule field.
   *
   * @param queryRule query rule
   * @param entityType entity type
   * @return an attribute or {@code null} if the query rule field is {@code null}
   * @throws UnknownAttributeException if the query rule field does not refer to an attribute
   */
  public static Attribute getQueryRuleAttribute(QueryRule queryRule, EntityType entityType) {
    String queryRuleField = queryRule.getField();
    if (queryRuleField == null) {
      return null;
    }

    Attribute attr = null;
    String[] queryRuleFieldTokens = StringUtils.split(queryRuleField, NESTED_ATTRIBUTE_SEPARATOR);
    EntityType entityTypeAtCurrentDepth = entityType;
    for (int depth = 0; depth < queryRuleFieldTokens.length; ++depth) {
      String attrName = queryRuleFieldTokens[depth];
      attr = entityTypeAtCurrentDepth.getAttribute(attrName);
      if (attr == null) {
        throw new UnknownAttributeException(entityTypeAtCurrentDepth, attrName);
      }
      if (depth + 1 < queryRuleFieldTokens.length) {
        entityTypeAtCurrentDepth = attr.getRefEntity();
      }
    }
    return attr;
  }

  /**
   * @param queryRuleField query rule field name, e.g. grandparent.parent.child
   * @param entityType entity type
   * @return attribute path
   * @throws UnknownAttributeException if no attribute exists for a query rule field name part
   * @throws MolgenisQueryException if query rule field is an invalid attribute path
   */
  public static List<Attribute> getAttributePath(String queryRuleField, EntityType entityType) {
    ImmutableList<Attribute> attributePath;

    if (queryRuleField.indexOf('.') == -1) {
      attributePath = ImmutableList.of(entityType.getAttributeByName(queryRuleField));
    } else {
      String[] tokens = queryRuleField.split("\\.");

      @SuppressWarnings("UnstableApiUsage")
      Builder<Attribute> builder = ImmutableList.builderWithExpectedSize(tokens.length);

      EntityType entityTypeAtDepth = entityType;
      for (int depth = 0; depth < tokens.length; ++depth) {
        String attributeName = tokens[depth];
        Attribute attribute = entityTypeAtDepth.getAttributeByName(attributeName);
        builder.add(attribute);

        if (depth + 1 < tokens.length) {
          if (!attribute.hasRefEntity()) {
            throw new MolgenisQueryException(
                format(
                    "Invalid query field [%s]: attribute [%s] does not refer to another entity",
                    queryRuleField, attribute.getName()));
          }
          entityTypeAtDepth = attribute.getRefEntity();
        }
      }

      attributePath = builder.build();
    }

    return attributePath;
  }

  public static List<Attribute> getAttributePathExpanded(
      String queryRuleField, EntityType entityType) {
    return getAttributePathExpanded(queryRuleField, entityType, false);
  }

  /**
   * Same as {@link #getAttributePath(String, EntityType)} but adds an id attribute to the path is
   * the last element is a reference attribute.
   *
   * @param queryRuleField query rule field name, e.g. grandparent.parent.child
   * @param entityType entity type
   * @param expandLabelInsteadOfId expand with label attribute instead of id attribute
   * @return attribute path
   * @throws UnknownAttributeException if no attribute exists for a query rule field name part
   * @throws MolgenisQueryException if query rule field is an invalid attribute path
   */
  public static List<Attribute> getAttributePathExpanded(
      String queryRuleField, EntityType entityType, boolean expandLabelInsteadOfId) {
    List<Attribute> attributePath = getAttributePath(queryRuleField, entityType);

    List<Attribute> expandedAttributePath;
    Attribute attribute = attributePath.get(attributePath.size() - 1);
    if (attribute.hasRefEntity()) {
      Attribute expandedAttribute;
      if (expandLabelInsteadOfId) {
        expandedAttribute = attribute.getRefEntity().getLabelAttribute();
      } else {
        expandedAttribute = attribute.getRefEntity().getIdAttribute();
      }

      @SuppressWarnings("UnstableApiUsage")
      Builder<Attribute> builder = ImmutableList.builderWithExpectedSize(attributePath.size() + 1);
      builder.addAll(attributePath);
      builder.add(expandedAttribute);

      expandedAttributePath = builder.build();
    } else {
      expandedAttributePath = attributePath;
    }
    return expandedAttributePath;
  }
}

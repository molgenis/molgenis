package org.molgenis.api.model;

import static java.lang.String.format;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class QueryRule {
  private Operator operator;
  private String field = null;
  private List<String> values = null;

  private List<QueryRule> nestedRules;

  public QueryRule(List<QueryRule> nestedRules) {
    this.nestedRules = nestedRules;
    this.operator = Operator.NESTED;
  }

  public QueryRule(Operator operator) {
    this.operator = operator;
  }

  /**
   * Standard constructor.
   *
   * <p>With this constructor the field, operator and value are set in one go, so there is no need
   * for additional statements.
   *
   * @param field The field-name.
   * @param operator The operator to use for comparing entries in the field with the value.
   * @param values The value.
   */
  public QueryRule(String field, Operator operator, List<String> values) {
    this.field = field;
    this.operator = operator;
    this.values = values;
  }

  /**
   * Constructor for search without attribute.
   *
   * <p>With this constructor the field, operator and value are set in one go, so there is no need
   * for additional statements.
   *
   * @param operator The operator to use for comparing entries in the field with the value.
   * @param values The value.
   */
  public QueryRule(Operator operator, List<String> values) {
    if (operator != Operator.SEARCH) {
      throw new IllegalArgumentException(
          format("QueryRule() without fieldName can only be applied for search"));
    }
    this.field = "*";
    this.operator = operator;
    this.values = values;
  }

  /** Different types of rules that can be applied. */
  public enum Operator {
    /** 'field' like 'value', searches all fields if field is not defined */
    SEARCH("search"),

    /**
     * 'field' equal to 'value'
     *
     * <p>When 'field type' is 'Mref' its results are derived from the 'Contains' behavior. <br>
     * Examples: <br>
     * 1. ref1 OR ref2 can result in:
     *
     * <ul>
     *   <li>re1
     *   <li>ref1, ref2
     *   <li>ref1, ref2, ref3;
     *   <li>ref2
     *   <li>ref2, ref3
     * </ul>
     *
     * 2. ref1 AND ref2 can result in:
     *
     * <ul>
     *   <li>ref1, ref2
     *   <li>ref1, ref2, ref3
     * </ul>
     */
    EQUALS("="),

    /** 'field' in 'value' (value being a list). */
    IN("IN"),

    /** 'field' less-than 'value' */
    LESS("<"),

    /** 'field' equal-or-less-than 'value' */
    LESS_EQUAL("<="),

    /** 'field' greater-than 'value' */
    GREATER(">"),

    /** 'field' equal-or-greater-than 'value' */
    GREATER_EQUAL(">="),

    /**
     * 'field' equal-or-greater-than 'from value' and equal-or-less-than 'to value' (value being a
     * list with 'from value' as first element and 'to value' as second element
     */
    RANGE("RANGE"),

    /** 'field' like 'value' (works like equals with wildcard before and after value) */
    LIKE("LIKE"),

    /** 'field' not-equal to 'value' */
    NOT("!="),

    /** AND operation */
    AND("AND"),

    /** OR operation */
    OR("OR"),

    /** indicates that 'value' is a nested array of QueryRule. The parameter 'field' is ommitted. */
    NESTED(""),

    /** Boolean query */
    SHOULD("SHOULD"),

    /** Disjunction max query */
    DIS_MAX("DIS_MAX"),

    /** Fuzzy match operator */
    FUZZY_MATCH("FUZZY_MATCH"),

    /** Fuzzy match operator */
    FUZZY_MATCH_NGRAM("FUZZY_MATCH_NGRAM");

    private final String label;

    /**
     * Translate String label of the operator to Operator.
     *
     * @param label of the operator
     */
    Operator(String label) {
      this.label = label;
    }

    /** Get the String label of the Operator. */
    @Override
    public String toString() {
      return label;
    }
  }

  /**
   * Returns the field-name set for this rule.
   *
   * @return The field-name.
   */
  public String getField() {
    return field;
  }

  /**
   * Returns the operator set for this rule.
   *
   * @return The operator.
   */
  public Operator getOperator() {
    return operator;
  }

  /**
   * Returns the value set for this rule.
   *
   * @return The value.
   */
  public List<String> getValue() {
    return values;
  }

  /**
   * Convenience function to return value as nested rule array.
   *
   * @return Nested rule set
   */
  public List<QueryRule> getNestedRules() {
    if (nestedRules == null) {
      return Collections.emptyList();
    }

    return nestedRules;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryRule queryRule = (QueryRule) o;
    return getOperator() == queryRule.getOperator()
        && Objects.equals(getField(), queryRule.getField())
        && Objects.equals(values, queryRule.values)
        && Objects.equals(getNestedRules(), queryRule.getNestedRules());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOperator(), getField(), values, getNestedRules());
  }

  @Override
  public String toString() {
    return "QueryRule{"
        + "operator="
        + operator
        + ", field='"
        + field
        + '\''
        + ", values="
        + values
        + ", nestedRules="
        + nestedRules
        + '}';
  }
}

package org.molgenis.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.molgenis.api.model.QueryRule.Operator;

public class Query {
  private final List<List<QueryRule>> rules = new ArrayList<>();

  public Query() {
    this.rules.add(new ArrayList<>());
  }

  public List<QueryRule> getRules() {
    if (this.rules.size() > 1)
      throw new IllegalArgumentException("Nested query not closed, use unnest() or unnestAll()");

    if (!this.rules.isEmpty()) {
      List<QueryRule> queryRules = this.rules.get(this.rules.size() - 1);

      return Collections.unmodifiableList(queryRules);
    } else return Collections.emptyList();
  }

  public Query or() {
    rules.get(this.rules.size() - 1).add(new QueryRule(Operator.OR));
    return this;
  }

  public Query and() {
    rules.get(this.rules.size() - 1).add(new QueryRule(Operator.AND));
    return this;
  }

  public Query not() {
    rules.get(this.rules.size() - 1).add(new QueryRule(Operator.NOT));
    return this;
  }

  public Query nest() {
    // add element to our nesting list...
    this.rules.add(new ArrayList<>());
    return this;
  }

  public Query unnest() {
    if (this.rules.size() == 1)
      throw new IllegalArgumentException("Cannot unnest root element of query");

    // remove last element and add to parent as nested rule
    QueryRule nested = new QueryRule(this.rules.get(this.rules.size() - 1));
    this.rules.remove(this.rules.get(this.rules.size() - 1));
    this.rules.get(this.rules.size() - 1).add(nested);
    return this;
  }

  public Query addRule(String field, Operator operator, List<String> values) {
    rules.get(this.rules.size() - 1).add(new QueryRule(field, operator, values));
    return this;
  }

  public void search(List<String> values) {
    rules.get(this.rules.size() - 1).add(new QueryRule(Operator.SEARCH, values));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Query query = (Query) o;
    return getRules().equals(query.getRules());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRules());
  }

  @Override
  public String toString() {
    return "Query{" + "rules=" + rules + '}';
  }
}

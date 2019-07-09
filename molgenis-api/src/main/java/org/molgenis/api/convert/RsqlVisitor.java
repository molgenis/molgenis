package org.molgenis.api.convert;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import java.util.Iterator;
import java.util.List;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.QueryRule.Operator;

/**
 * RSQLVisitor implementation that creates {@link Query} objects for an RSQL tree.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
public class RsqlVisitor extends NoArgRSQLVisitorAdapter<Query> {
  private Query q;

  private void initQuery() {
    if (q == null) {
      q = new Query();
    }
  }

  @Override
  public Query visit(AndNode node) {
    initQuery();
    boolean nested = node.getChildren().size() > 1;
    if (nested) {
      q.nest();
    }
    for (Iterator<Node> it = node.iterator(); it.hasNext(); ) {
      Node child = it.next();
      child.accept(this);

      if (it.hasNext()) {
        q.and();
      }
    }
    if (nested) {
      q.unnest();
    }
    return q;
  }

  @Override
  public Query visit(OrNode node) {
    initQuery();
    boolean nested = node.getChildren().size() > 1;
    if (nested) {
      q.nest();
    }

    for (Iterator<Node> it = node.iterator(); it.hasNext(); ) {
      Node child = it.next();
      child.accept(this);

      if (it.hasNext()) {
        q.or();
      }
    }
    if (nested) {
      q.unnest();
    }

    return q;
  }

  @Override
  public Query visit(ComparisonNode node) {
    initQuery();
    String attrName = node.getSelector();
    String symbol = node.getOperator().getSymbol();
    List<String> values = node.getArguments();
    switch (symbol) {
      case "==":
        validateNrOfValues(values, Operator.EQUALS, 1);
        q.addRule(attrName, Operator.EQUALS, values);
        break;
      case "=in=":
        q.addRule(attrName, Operator.IN, values);
        break;
      case "=lt=":
      case "<":
        validateNrOfValues(values, Operator.LESS, 1);
        q.addRule(attrName, Operator.LESS, values);
        break;
      case "=le=":
      case "<=":
        validateNrOfValues(values, Operator.LESS_EQUAL, 1);
        q.addRule(attrName, Operator.LESS_EQUAL, values);
        break;
      case "=gt=":
      case ">":
        validateNrOfValues(values, Operator.GREATER, 1);
        q.addRule(attrName, Operator.GREATER, values);
        break;
      case "=ge=":
      case ">=":
        validateNrOfValues(values, Operator.GREATER_EQUAL, 1);
        q.addRule(attrName, Operator.GREATER_EQUAL, values);
        break;
      case "=rng=":
        validateNrOfValues(values, Operator.RANGE, 2);
        q.addRule(attrName, Operator.RANGE, values);
        break;
      case "=like=":
        validateNrOfValues(values, Operator.LIKE, 1);
        q.addRule(attrName, Operator.LIKE, values);
        break;
      case "=q=":
        validateNrOfValues(values, Operator.SEARCH, 1);
        if (attrName.equals("*")) {
          q.search(values);
        } else {
          q.addRule(attrName, Operator.SEARCH, values);
        }
        break;
      case "=notlike=":
      case "!=":
        validateNrOfValues(values, Operator.LIKE, 1);
        q.not().addRule(attrName, Operator.LIKE, values);
        break;
      case "=should=":
      case "=dismax=":
      case "=fuzzy=":
        throw new IllegalArgumentException(
            "Unsupported RSQL query operator [" + symbol + "]"); // FIXME coded
      default:
        throw new IllegalArgumentException(
            "Unknown RSQL query operator [" + symbol + "]"); // FIXME coded
    }
    return q;
  }

  private void validateNrOfValues(List<String> values, Operator operator, int expectedNrValues) {
    int nrOfValues = values.size();
    if (nrOfValues != expectedNrValues) {
      throw new RuntimeException(
          "nrOfValues: "
              + nrOfValues
              + " expectedNrValues: "
              + expectedNrValues
              + " operator: "
              + operator); // FIXME:coded
    }
  }
}

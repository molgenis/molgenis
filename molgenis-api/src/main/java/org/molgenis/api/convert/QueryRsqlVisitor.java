package org.molgenis.api.convert;

import static java.util.stream.Collectors.toList;
import static org.molgenis.api.model.Query.Operator.AND;
import static org.molgenis.api.model.Query.Operator.CONTAINS;
import static org.molgenis.api.model.Query.Operator.EQUALS;
import static org.molgenis.api.model.Query.Operator.GREATER_THAN;
import static org.molgenis.api.model.Query.Operator.GREATER_THAN_OR_EQUAL_TO;
import static org.molgenis.api.model.Query.Operator.IN;
import static org.molgenis.api.model.Query.Operator.LESS_THAN;
import static org.molgenis.api.model.Query.Operator.LESS_THAN_OR_EQUAL_TO;
import static org.molgenis.api.model.Query.Operator.MATCHES;
import static org.molgenis.api.model.Query.Operator.NOT_EQUALS;
import static org.molgenis.api.model.Query.Operator.NOT_IN;
import static org.molgenis.api.model.Query.Operator.OR;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.OrNode;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.util.UnexpectedEnumException;

public class QueryRsqlVisitor extends NoArgRSQLVisitorAdapter<Query> {
  @Override
  public Query visit(AndNode node) {
    return visit(node, AND);
  }

  @Override
  public Query visit(OrNode node) {
    return visit(node, OR);
  }

  private Query visit(LogicalNode node, Operator operator) {
    List<Query> subQueries =
        node.getChildren().stream().map(childNode -> childNode.accept(this)).collect(toList());
    return Query.builder().setOperator(operator).setValue(subQueries).build();
  }

  @Override
  public Query visit(ComparisonNode node) {
    String item = toItem(node);
    Operator operator = toOperator(node);
    Object value = toValue(node, operator);
    return Query.builder().setItem(item).setOperator(operator).setValue(value).build();
  }

  private static @Nullable @CheckForNull String toItem(ComparisonNode node) {
    String selector = node.getSelector();
    return selector.equals("*") ? null : selector;
  }

  private static Operator toOperator(ComparisonNode node) {
    Operator operator;

    String symbol = node.getOperator().getSymbol();
    switch (symbol) {
      case "==":
        operator = EQUALS;
        break;
      case "!=":
        operator = NOT_EQUALS;
        break;
      case "=in=":
        operator = IN;
        break;
      case "=out=":
        operator = NOT_IN;
        break;
      case "=lt=":
      case "<":
        operator = LESS_THAN;
        break;
      case "=le=":
      case "<=":
        operator = LESS_THAN_OR_EQUAL_TO;
        break;
      case "=gt=":
      case ">":
        operator = GREATER_THAN;
        break;
      case "=ge=":
      case ">=":
        operator = GREATER_THAN_OR_EQUAL_TO;
        break;
      case "=like=":
        operator = CONTAINS;
        break;
      case "=q=":
        operator = MATCHES;
        break;
      default:
        throw new IllegalArgumentException("Unknown RSQL query operator [" + symbol + "]");
    }
    return operator;
  }

  private static Object toValue(ComparisonNode node, Operator operator) {
    Object value;

    List<String> arguments = getNormalizedArguments(node);
    switch (operator) {
      case EQUALS:
      case NOT_EQUALS:
        value = arguments.get(0);
        break;
      case MATCHES:
      case CONTAINS:
      case LESS_THAN:
      case LESS_THAN_OR_EQUAL_TO:
      case GREATER_THAN:
      case GREATER_THAN_OR_EQUAL_TO:
        value = arguments.get(0);
        if (value == null) {
          throw new MissingRsqlValueException(operator);
        }
        break;
      case IN:
      case NOT_IN:
        value = arguments;
        break;
      case AND:
      case OR:
      default:
        throw new UnexpectedEnumException(operator);
    }

    return value;
  }

  private static List<String> getNormalizedArguments(ComparisonNode node) {
    return node.getArguments().stream()
        .map(argument -> "".equals(argument) ? null : argument)
        .collect(toList());
  }
}

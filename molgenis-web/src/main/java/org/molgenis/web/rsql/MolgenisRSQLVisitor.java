package org.molgenis.web.rsql;

import static java.util.stream.Collectors.toList;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import java.util.Iterator;
import java.util.List;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.web.exception.UnsupportedRsqlOperationException;
import org.molgenis.web.exception.UnsupportedRsqlOperatorException;

/**
 * RSQLVisitor implementation that creates {@link Query} objects for an RSQL tree.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
public class MolgenisRSQLVisitor extends NoArgRSQLVisitorAdapter<Query<Entity>> {
  private QueryImpl<Entity> q;
  private final Repository<Entity> repository;
  private final RSQLValueParser rsqlValueParser;

  public MolgenisRSQLVisitor(Repository<Entity> repository) {
    this(repository, new RSQLValueParser());
  }

  public MolgenisRSQLVisitor(Repository<Entity> repository, RSQLValueParser rsqlValueParser) {
    this.repository = repository;
    this.rsqlValueParser = rsqlValueParser;
  }

  private void initQuery() {
    if (q == null) {
      q = new QueryImpl<>(repository);
    }
  }

  @Override
  public Query<Entity> visit(AndNode node) {
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
  public Query<Entity> visit(OrNode node) {
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
  public Query<Entity> visit(ComparisonNode node) {
    initQuery();
    String attrName = node.getSelector();
    String symbol = node.getOperator().getSymbol();
    List<String> values = node.getArguments();
    switch (symbol) {
      case "=notlike=":
        String notLikeValue = values.get(0);
        q.not().like(attrName, notLikeValue);
        break;
      case "=q=":
        String searchValue = values.get(0);
        if (attrName.equals("*")) {
          q.search(searchValue);
        } else {
          q.search(attrName, searchValue);
        }
        break;
      case "=sq=":
        String query = values.get(0);
        if (attrName.equals("*")) {
          q.searchQuery(query);
        } else {
          q.searchQuery(attrName, query);
        }
        break;
      case "==":
        Object eqValue = rsqlValueParser.parse(values.get(0), getAttribute(node));
        q.eq(attrName, eqValue);
        break;
      case "=in=":
        var inAttr = getAttribute(node);
        q.in(
            attrName,
            values.stream().map(value -> rsqlValueParser.parse(value, inAttr)).collect(toList()));
        break;
      case "=lt=":
      case "<":
        var ltAttr = getAttribute(node);
        validateNumericOrDate(ltAttr, symbol);
        Object ltValue = rsqlValueParser.parse(values.get(0), ltAttr);
        q.lt(attrName, ltValue);
        break;
      case "=le=":
      case "<=":
        var leAttr = getAttribute(node);
        validateNumericOrDate(leAttr, symbol);
        Object leValue = rsqlValueParser.parse(values.get(0), leAttr);
        q.le(attrName, leValue);
        break;
      case "=gt=":
      case ">":
        var gtAttr = getAttribute(node);
        validateNumericOrDate(gtAttr, symbol);
        Object gtValue = rsqlValueParser.parse(values.get(0), gtAttr);
        q.gt(attrName, gtValue);
        break;
      case "=ge=":
      case ">=":
        var geAttr = getAttribute(node);
        validateNumericOrDate(geAttr, symbol);
        Object geValue = rsqlValueParser.parse(values.get(0), geAttr);
        q.ge(attrName, geValue);
        break;
      case "=rng=":
        var rngAttr = getAttribute(node);
        validateNumericOrDate(rngAttr, symbol);
        Object fromValue =
            values.get(0) != null ? rsqlValueParser.parse(values.get(0), rngAttr) : null;
        Object toValue =
            values.get(1) != null ? rsqlValueParser.parse(values.get(1), rngAttr) : null;
        q.rng(attrName, fromValue, toValue);
        break;
      case "=like=":
        String likeValue = values.get(0);
        q.like(attrName, likeValue);
        break;
      case "!=":
        Object notEqValue = rsqlValueParser.parse(values.get(0), getAttribute(node));
        q.not().eq(attrName, notEqValue);
        break;
      case "=should=":
      case "=dismax=":
      case "=fuzzy=":
        throw new UnsupportedRsqlOperatorException(symbol);
      default:
        throw new IllegalStateException(String.format("Unknown rsql operator: %s", symbol));
    }
    return q;
  }

  private void validateNumericOrDate(Attribute attr, String symbol) {
    switch (attr.getDataType()) {
      case DATE:
      case DATE_TIME:
      case DECIMAL:
      case INT:
      case LONG:
        break;
        // $CASES-OMITTED$
      default:
        throw new UnsupportedRsqlOperationException(symbol, repository.getEntityType(), attr);
    }
  }

  private Attribute getAttribute(ComparisonNode node) {
    var entityType = repository.getEntityType();
    String attrName = node.getSelector();

    String[] attrTokens = attrName.split("\\.");
    var attr = entityType.getAttribute(attrTokens[0]);
    if (attr == null) {
      throw new UnknownAttributeException(entityType, attrName);
    }
    EntityType entityTypeAtDepth;
    for (var i = 1; i < attrTokens.length; ++i) {
      entityTypeAtDepth = attr.getRefEntity();
      attr = entityTypeAtDepth.getAttribute(attrTokens[i]);
      if (attr == null) {
        throw new UnknownAttributeException(entityTypeAtDepth, attrName);
      }
    }

    return attr;
  }
}

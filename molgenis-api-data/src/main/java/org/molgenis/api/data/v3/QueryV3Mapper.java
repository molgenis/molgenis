package org.molgenis.api.data.v3;

import static java.util.stream.Collectors.toList;
import static org.molgenis.api.model.QueryRule.Operator.GREATER;
import static org.molgenis.api.model.QueryRule.Operator.GREATER_EQUAL;
import static org.molgenis.api.model.QueryRule.Operator.LESS;
import static org.molgenis.api.model.QueryRule.Operator.LESS_EQUAL;
import static org.molgenis.api.model.QueryRule.Operator.RANGE;

import java.util.List;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.QueryRule;
import org.molgenis.api.model.QueryRule.Operator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.UnexpectedEnumException;
import org.molgenis.web.rsql.RSQLValueParser;

public class QueryV3Mapper {

  private final RSQLValueParser rsqlValueParser = new RSQLValueParser();

  public org.molgenis.data.Query map(Query query, Repository<Entity> repository) {
    org.molgenis.data.Query newQuery = new QueryImpl<>(repository);
    for (QueryRule rule : query.getRules()) {
      mapRule(rule, repository.getEntityType(), newQuery);
    }
    return newQuery;
  }

  private void mapRule(
      QueryRule rule, EntityType entityType, org.molgenis.data.Query molgenisQuery) {
    String attrName = rule.getField();
    Operator operator = rule.getOperator();
    List<String> values = rule.getValue();

    boolean isNested = false;
    switch (operator) {
      case SEARCH:
        String searchValue = values.get(0);
        if (attrName.equals("*")) {
          molgenisQuery.search(searchValue);
        } else {
          molgenisQuery.search(attrName, searchValue);
        }
        break;
      case EQUALS:
        Object eqValue = rsqlValueParser.parse(values.get(0), getAttribute(entityType, attrName));
        molgenisQuery.eq(attrName, eqValue);
        break;
      case IN:
        Attribute inAttr = getAttribute(entityType, attrName);
        molgenisQuery.in(
            attrName,
            values.stream().map(value -> rsqlValueParser.parse(value, inAttr)).collect(toList()));
        break;
      case LESS:
        Attribute ltAttr = getAttribute(entityType, attrName);
        validateNumericOrDate(ltAttr, LESS);
        Object ltValue = rsqlValueParser.parse(values.get(0), ltAttr);
        molgenisQuery.lt(attrName, ltValue);
        break;
      case LESS_EQUAL:
        Attribute leAttr = getAttribute(entityType, attrName);
        validateNumericOrDate(leAttr, LESS_EQUAL);
        Object leValue = rsqlValueParser.parse(values.get(0), leAttr);
        molgenisQuery.le(attrName, leValue);
        break;
      case GREATER:
        Attribute gtAttr = getAttribute(entityType, attrName);
        validateNumericOrDate(gtAttr, GREATER);
        Object gtValue = rsqlValueParser.parse(values.get(0), gtAttr);
        molgenisQuery.gt(attrName, gtValue);
        break;
      case GREATER_EQUAL:
        Attribute geAttr = getAttribute(entityType, attrName);
        validateNumericOrDate(geAttr, GREATER_EQUAL);
        Object geValue = rsqlValueParser.parse(values.get(0), geAttr);
        molgenisQuery.ge(attrName, geValue);
        break;
      case RANGE:
        Attribute rngAttr = getAttribute(entityType, attrName);
        validateNumericOrDate(rngAttr, RANGE);
        Object fromValue =
            values.get(0) != null ? rsqlValueParser.parse(values.get(0), rngAttr) : null;
        Object toValue =
            values.get(1) != null ? rsqlValueParser.parse(values.get(1), rngAttr) : null;
        molgenisQuery.rng(attrName, fromValue, toValue);
        break;
      case LIKE:
        String likeValue = values.get(0);
        molgenisQuery.like(attrName, likeValue);
        break;
      case NOT:
        molgenisQuery.not();
        break;
      case AND:
        molgenisQuery.and();
        break;
      case OR:
        molgenisQuery.or();
        break;
      case NESTED:
        isNested = true;
        break;
      case SHOULD:
      case DIS_MAX:
      case FUZZY_MATCH:
      case FUZZY_MATCH_NGRAM:
      default:
        throw new UnexpectedEnumException(operator);
    }
    if (isNested) {
      molgenisQuery.nest();
      for (QueryRule nested : rule.getNestedRules()) {
        mapRule(nested, entityType, molgenisQuery);
      }
      molgenisQuery.unnest();
    }
  }

  private void validateNumericOrDate(Attribute attr, Operator operator) {
    switch (attr.getDataType()) {
      case DATE:
      case DATE_TIME:
      case DECIMAL:
      case INT:
      case LONG:
        break;
        // $CASES-OMITTED$
      default:
        throw new IllegalArgumentException(
            "Can't perform operator "
                + operator
                + " on attribute '\""
                + attr.getName()
                + "\""); // FIXME: coded exception
    }
  }

  private Attribute getAttribute(EntityType entityType, String attrName) {
    String[] attrTokens = attrName.split("\\.");
    Attribute attr = entityType.getAttribute(attrTokens[0]);
    if (attr == null) {
      throw new UnknownAttributeException(entityType, attrName);
    }
    EntityType entityTypeAtDepth;
    for (int i = 1; i < attrTokens.length; ++i) {
      entityTypeAtDepth = attr.getRefEntity();
      attr = entityTypeAtDepth.getAttribute(attrTokens[i]);
      if (attr == null) {
        throw new UnknownAttributeException(entityTypeAtDepth, attrName);
      }
    }

    return attr;
  }
}

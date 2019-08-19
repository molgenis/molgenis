package org.molgenis.api.convert;

import static org.molgenis.api.model.Sort.Order.Direction.ASC;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.util.UnexpectedEnumException;

public class UrlParameterUtils {

  public static final String PAGE = "page";
  public static final String SIZE = "size";
  public static final String QUERY = "q";
  public static final String SORT = "sort";
  public static final String SELECTION = "selection";
  public static final String FILTER = "filter";

  private UrlParameterUtils() {
    // emptu constructor to hide default.
  }

  public static Map<String, String> createUrlParams(
      Selection filter, Selection expand, Sort sort, int size, int number, Optional<Query> query) {
    Map<String, String> params = new HashMap<>();
    String filterParam = selectionToUrlParam(filter);
    if (!filterParam.isEmpty()) {
      params.put(FILTER, filterParam);
    }
    String selectionParam = selectionToUrlParam(expand);
    if (!selectionParam.isEmpty()) {
      params.put(SELECTION, selectionParam);
    }
    String sortParam = sortToUrlParam(sort);
    if (!sortParam.isEmpty()) {
      params.put(SORT, sortParam);
    }
    String queryParam = query.isPresent() ? queryToUrlParam(query.get()) : "";
    if (!queryParam.isEmpty()) {
      params.put(QUERY, queryParam);
    }

    params.put(PAGE, Integer.toString(number));
    params.put(SIZE, Integer.toString(size));

    return params;
  }

  public static String createUrlParamString(
      Selection filter, Selection expand, Sort sort, int size, int number, Optional<Query> query) {
    Map<String, String> paramMap = createUrlParams(filter, expand, sort, size, number, query);
    return paramMap.entrySet().stream()
        .filter(entry -> !entry.getValue().isEmpty())
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("&"));
  }

  static String queryToUrlParam(Query query) {
    return queryToUrlParam(query, false);
  }

  static String queryToUrlParam(Query query, boolean hasParent) {
    String param;
    Operator queryOperator = query.getOperator();
    switch (queryOperator) {
      case EQUALS:
        param = query.getItem() + "==" + query.getStringValue();
        break;
      case NOT_EQUALS:
        param = query.getItem() + "!=" + query.getStringValue();
        break;
      case IN:
        param =
            query.getItem()
                + "=in="
                + query.getStringListValue().stream().collect(Collectors.joining(","));
        break;
      case NOT_IN:
        param =
            query.getItem()
                + "=out="
                + query.getStringListValue().stream().collect(Collectors.joining(","));
        break;
      case LESS_THAN:
        param = query.getItem() + "<" + query.getStringValue();
        break;
      case LESS_THAN_OR_EQUAL_TO:
        param = query.getItem() + "<=" + query.getStringValue();
        break;
      case GREATER_THAN:
        param = query.getItem() + ">" + query.getStringValue();
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        param = query.getItem() + ">=" + query.getStringValue();
        break;
      case CONTAINS:
        param = query.getItem() + "=like=" + query.getStringValue();
        break;
      case MATCHES:
        param = query.getItem() + "=q=" + query.getStringValue();
        break;
      case AND:
        String andQuery =
            query.getQueryListValue().stream()
                .map(subQuery -> queryToUrlParam(subQuery, true))
                .collect(Collectors.joining(";"));
        param = hasParent ? "(" + andQuery + ")" : andQuery;
        break;
      case OR:
        String orQuery =
            query.getQueryListValue().stream()
                .map(subQuery -> queryToUrlParam(subQuery, true))
                .collect(Collectors.joining(","));
        param = hasParent ? "(" + orQuery + ")" : orQuery;
        break;
      default:
        throw new UnexpectedEnumException(queryOperator);
    }
    return param;
  }

  static String sortToUrlParam(Sort sort) {
    return sort.getOrders().stream()
        .map(order -> (order.getDirection() == ASC ? "+" : "-") + order.getItem())
        .collect(Collectors.joining(","));
  }

  static String selectionToUrlParam(Selection selection) {
    StringBuilder stringBuilder = new StringBuilder();
    Map<String, Selection> itemSelections = selection.getItemSelections();
    if (itemSelections != null) {
      for (Entry<String, Selection> entry : itemSelections.entrySet()) {
        if (stringBuilder.length() != 0) {
          stringBuilder.append(",");
        }
        stringBuilder.append(entry.getKey());
        if (entry.getValue() != null) {
          stringBuilder.append("(");
          stringBuilder.append(selectionToUrlParam(entry.getValue()));
          stringBuilder.append(")");
        }
      }
    }
    return stringBuilder.toString();
  }
}

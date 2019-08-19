package org.molgenis.api.convert;

import static org.molgenis.api.model.Sort.Order.Direction.ASC;
import static org.molgenis.api.model.Sort.Order.Direction.DESC;
import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order;
import org.testng.annotations.Test;

public class UrlParameterUtilsTest {

  @Test
  public void testSelectionToUrlParam() {
    Map<String, Selection> subSubItemSelections = new HashMap<>();
    subSubItemSelections.put("jkl", null);
    Selection subSubItemSelection = new Selection(subSubItemSelections);
    Map<String, Selection> subItemSelections = new HashMap<>();
    subItemSelections.put("ghi", subSubItemSelection);
    Selection subItemSelection = new Selection(subItemSelections);
    Map<String, Selection> itemSelections = new HashMap<>();
    itemSelections.put("abc", null);
    itemSelections.put("def", subItemSelection);
    Selection itemSelection = new Selection(itemSelections);

    assertEquals(UrlParameterUtils.selectionToUrlParam(itemSelection), "def(ghi(jkl)),abc");
  }

  @Test
  public void testSelectionToUrlParamEmpty() {
    assertEquals(UrlParameterUtils.selectionToUrlParam(new Selection()), "");
  }

  @Test
  public void testSortToUrlParam() {
    List<Order> orders = new ArrayList<>();
    orders.add(Order.create("test1", ASC));
    orders.add(Order.create("test2", DESC));
    orders.add(Order.create("test3", ASC));
    Sort sort = Sort.create(orders);

    assertEquals(UrlParameterUtils.sortToUrlParam(sort), "+test1,-test2,+test3");
  }

  @Test
  public void testSortToUrlParamEmpty() {
    assertEquals(UrlParameterUtils.sortToUrlParam(Sort.EMPTY_SORT), "");
  }

  @Test
  public void testQueryToUrlParamContains() {
    Query query = Query.create("test", Operator.CONTAINS, "value");
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test=like=value");
  }

  @Test
  public void testQueryToUrlParamMatches() {
    Query query = Query.create("test", Operator.MATCHES, "value");
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test=q=value");
  }

  @Test
  public void testQueryToUrlParamEqual() {
    Query query = Query.create("test", Operator.EQUALS, "value");
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test==value");
  }

  @Test
  public void testQueryToUrlParamGreater() {
    Query query = Query.create("test", Operator.GREATER_THAN, "value");
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test>value");
  }

  @Test
  public void testQueryToUrlParamGreaterEqual() {
    Query query = Query.create("test", Operator.GREATER_THAN_OR_EQUAL_TO, "value");
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test>=value");
  }

  @Test
  public void testQueryToUrlParamLess() {
    Query query = Query.create("test", Operator.LESS_THAN, "value");
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test<value");
  }

  @Test
  public void testQueryToUrlParamLessEqual() {
    Query query = Query.create("test", Operator.LESS_THAN_OR_EQUAL_TO, "value");
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test<=value");
  }

  @Test
  public void testQueryToUrlParamNotEqual() {
    Query query = Query.create("test", Operator.NOT_EQUALS, "value");
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test!=value");
  }

  @Test
  public void testQueryToUrlParamNotIn() {
    Query query = Query.create("test", Operator.NOT_IN, Arrays.asList("value1", "value2"));
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test=out=value1,value2");
  }

  @Test
  public void testQueryToUrlParamIn() {
    Query query = Query.create("test", Operator.IN, Arrays.asList("value1", "value2"));
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test=in=value1,value2");
  }

  @Test
  public void testQueryToUrlParamAnd() {
    Query query1 = Query.create("test1", Operator.CONTAINS, "value");
    Query query2 = Query.create("test2", Operator.CONTAINS, "value");
    Query query = Query.create(null, Operator.AND, Arrays.asList(query1, query2));
    assertEquals(UrlParameterUtils.queryToUrlParam(query), "test1=like=value;test2=like=value");
  }

  @Test
  public void testQueryToUrlParamOr() {
    Query subQuery1 = Query.create("test1", Operator.CONTAINS, "value");
    Query subQuery2 = Query.create("test2", Operator.CONTAINS, "value");
    Query query2 = Query.create(null, Operator.OR, Arrays.asList(subQuery1, subQuery2));
    Query query1 = Query.create("test1", Operator.CONTAINS, "value");
    Query query = Query.create(null, Operator.OR, Arrays.asList(query1, query2));
    assertEquals(
        UrlParameterUtils.queryToUrlParam(query),
        "test1=like=value,(test1=like=value,test2=like=value)");
  }

  @Test
  public void testCreateUrlParamString() {
    Query query = Query.create("test1", Operator.CONTAINS, "value");
    Selection itemSelection = new Selection(Collections.singletonMap("abc", null));
    Selection filterSelection = new Selection(Collections.singletonMap("def", null));
    Sort sort = Sort.create("test", ASC);

    assertEquals(
        UrlParameterUtils.createUrlParamString(
            filterSelection, itemSelection, sort, 100, 3, Optional.of(query)),
        "filter=def&q=test1=like=value&selection=abc&size=100&sort=+test&page=3");
  }
}

package org.molgenis.api.convert;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
import static org.testng.Assert.assertEquals;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.OrNode;
import java.util.Iterator;
import java.util.List;
import org.molgenis.api.model.Query;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class QueryRsqlVisitorTest extends AbstractMockitoTest {
  private QueryRsqlVisitor queryRsqlVisitor;

  @BeforeMethod
  public void beforeMethod() {
    queryRsqlVisitor = new QueryRsqlVisitor();
  }

  @Test
  public void testVisitComparisonNodeEquals() {
    String selector = "name";
    String argument = "piet";
    ComparisonOperator operator = new ComparisonOperator("==");
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query = Query.builder().setItem(selector).setOperator(EQUALS).setValue(argument).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @Test
  public void testVisitComparisonNodeEqualsNull() {
    String selector = "name";
    String argument = "";
    ComparisonOperator operator = new ComparisonOperator("==");
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query = Query.builder().setItem(selector).setOperator(EQUALS).setValue(null).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @Test
  public void testVisitComparisonNodeNotEquals() {
    String selector = "name";
    String argument = "piet";
    ComparisonOperator operator = new ComparisonOperator("!=");
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query =
        Query.builder().setItem(selector).setOperator(NOT_EQUALS).setValue(argument).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @Test
  public void testVisitComparisonNodeIn() {
    String selector = "name";
    List<String> arguments = asList("jan", "piet");
    ComparisonOperator operator = new ComparisonOperator("=in=", true);
    ComparisonNode node = new ComparisonNode(operator, selector, arguments);

    Query query = Query.builder().setItem(selector).setOperator(IN).setValue(arguments).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @Test
  public void testVisitComparisonNodeNotIn() {
    String selector = "name";
    List<String> arguments = asList("jan", "piet");
    ComparisonOperator operator = new ComparisonOperator("=out=", true);
    ComparisonNode node = new ComparisonNode(operator, selector, arguments);

    Query query = Query.builder().setItem(selector).setOperator(NOT_IN).setValue(arguments).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @Test
  public void testVisitComparisonNodeMatches() {
    String selector = "name";
    String argument = "piet";
    ComparisonOperator operator = new ComparisonOperator("=q=");
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query = Query.builder().setItem(selector).setOperator(MATCHES).setValue(argument).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testVisitComparisonNodeMatchesNull() {
    String selector = "name";
    String argument = "";
    ComparisonOperator operator = new ComparisonOperator("=q=");
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    queryRsqlVisitor.visit(node);
  }

  @Test
  public void testVisitComparisonNodeMatchesAllSelector() {
    String selector = "*";
    String argument = "piet";
    ComparisonOperator operator = new ComparisonOperator("=q=");
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query = Query.builder().setItem(null).setOperator(MATCHES).setValue(argument).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @Test
  public void testVisitComparisonNodeContains() {
    String selector = "name";
    String argument = "piet";
    ComparisonOperator operator = new ComparisonOperator("=like=");
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query =
        Query.builder().setItem(selector).setOperator(CONTAINS).setValue(argument).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @DataProvider(name = "testVisitComparisonNodeLessThanProvider")
  public static Iterator<Object[]> testVisitComparisonNodeLessThanProvider() {
    return asList(new Object[] {"<"}, new Object[] {"=lt="}).iterator();
  }

  @Test(dataProvider = "testVisitComparisonNodeLessThanProvider")
  public void testVisitComparisonNodeLessThan(String symbol) {
    String selector = "age";
    String argument = "87";
    ComparisonOperator operator = new ComparisonOperator(symbol);
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query =
        Query.builder().setItem(selector).setOperator(LESS_THAN).setValue(argument).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @DataProvider(name = "testVisitComparisonNodeLessThanOrEqualProvider")
  public static Iterator<Object[]> testVisitComparisonNodeLessThanOrEqualProvider() {
    return asList(new Object[] {"<="}, new Object[] {"=le="}).iterator();
  }

  @Test(dataProvider = "testVisitComparisonNodeLessThanOrEqualProvider")
  public void testVisitComparisonNodeLessThanOrEqual(String symbol) {
    String selector = "age";
    String argument = "87";
    ComparisonOperator operator = new ComparisonOperator(symbol);
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query =
        Query.builder()
            .setItem(selector)
            .setOperator(LESS_THAN_OR_EQUAL_TO)
            .setValue(argument)
            .build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @DataProvider(name = "testVisitComparisonNodeGreaterThanProvider")
  public static Iterator<Object[]> testVisitComparisonNodeGreaterThanProvider() {
    return asList(new Object[] {">"}, new Object[] {"=gt="}).iterator();
  }

  @Test(dataProvider = "testVisitComparisonNodeGreaterThanProvider")
  public void testVisitComparisonNodeGreaterThan(String symbol) {
    String selector = "age";
    String argument = "87";
    ComparisonOperator operator = new ComparisonOperator(symbol);
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query =
        Query.builder().setItem(selector).setOperator(GREATER_THAN).setValue(argument).build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @DataProvider(name = "testVisitComparisonNodeGreaterThanOrEqualProvider")
  public static Iterator<Object[]> testVisitComparisonNodeGreaterThanOrEqualProvider() {
    return asList(new Object[] {">="}, new Object[] {"=ge="}).iterator();
  }

  @Test(dataProvider = "testVisitComparisonNodeGreaterThanOrEqualProvider")
  public void testVisitComparisonNodeGreaterThanOrEqual(String symbol) {
    String selector = "age";
    String argument = "87";
    ComparisonOperator operator = new ComparisonOperator(symbol);
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    Query query =
        Query.builder()
            .setItem(selector)
            .setOperator(GREATER_THAN_OR_EQUAL_TO)
            .setValue(argument)
            .build();
    assertEquals(queryRsqlVisitor.visit(node), query);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testVisitComparisonNodeIllegalOperator() {
    String selector = "name";
    String argument = "piet";
    ComparisonOperator operator = new ComparisonOperator("=illegal=");
    ComparisonNode node = new ComparisonNode(operator, selector, singletonList(argument));

    queryRsqlVisitor.visit(node);
  }

  @Test
  public void testVisitOrNode() {
    String argument0 = "piet";
    ComparisonOperator operator0 = new ComparisonOperator("=q=");
    ComparisonNode node0 = new ComparisonNode(operator0, "*", singletonList(argument0));

    String argument1 = "jan";
    ComparisonOperator operator1 = new ComparisonOperator("=q=");
    ComparisonNode node1 = new ComparisonNode(operator1, "*", singletonList(argument1));

    OrNode orNode = new OrNode(asList(node0, node1));
    Query query =
        Query.builder()
            .setOperator(OR)
            .setValue(
                asList(
                    Query.builder().setOperator(MATCHES).setValue(argument0).build(),
                    Query.builder().setOperator(MATCHES).setValue(argument1).build()))
            .build();
    assertEquals(queryRsqlVisitor.visit(orNode), query);
  }

  @Test
  public void testVisitAndNode() {
    String argument0 = "piet";
    ComparisonOperator operator0 = new ComparisonOperator("=q=");
    ComparisonNode node0 = new ComparisonNode(operator0, "*", singletonList(argument0));

    String argument1 = "jan";
    ComparisonOperator operator1 = new ComparisonOperator("=q=");
    ComparisonNode node1 = new ComparisonNode(operator1, "*", singletonList(argument1));

    AndNode andNode = new AndNode(asList(node0, node1));
    Query query =
        Query.builder()
            .setOperator(AND)
            .setValue(
                asList(
                    Query.builder().setOperator(MATCHES).setValue(argument0).build(),
                    Query.builder().setOperator(MATCHES).setValue(argument1).build()))
            .build();
    assertEquals(queryRsqlVisitor.visit(andNode), query);
  }
}

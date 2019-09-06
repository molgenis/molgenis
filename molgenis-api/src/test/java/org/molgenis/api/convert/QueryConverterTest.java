package org.molgenis.api.convert;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import org.mockito.Mock;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class QueryConverterTest extends AbstractMockitoTest {
  @Mock private QueryRsqlVisitor rsqlVisitor;
  private QueryConverter queryConverter;

  @BeforeMethod
  public void setUpBeforeMethod() {
    // RSQLParser is final, can't be mocked
    queryConverter = new QueryConverter(new RSQLParser(), rsqlVisitor);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testRsqlConverter() {
    new QueryConverter(null, null);
  }

  @Test
  public void testCreateQuery() {
    String rsqlQuery = "item==value";
    ComparisonNode node =
        new ComparisonNode(new ComparisonOperator("=="), "item", singletonList("value"));
    Query query = Query.builder().setItem("item").setOperator(Operator.EQUALS).build();
    when(node.accept(rsqlVisitor)).thenReturn(query);
    assertEquals(queryConverter.convert(rsqlQuery), query);
    verify(rsqlVisitor).visit(node, null);
  }

  @Test(expectedExceptions = QueryParseException.class)
  public void testCreateQueryParseException() {
    String rsqlQuery = "illegalQuery";
    queryConverter.convert(rsqlQuery);
  }

  @Test(expectedExceptions = UnknownQueryOperatorException.class, expectedExceptionsMessageRegExp = "operator:=illegalOperator=")
  public void testCreateQueryUnknownQueryOperatorException() {
    String rsqlQuery = "item=illegalOperator=value";
    queryConverter.convert(rsqlQuery);
  }
}

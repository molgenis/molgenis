package org.molgenis.api.convert;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.test.AbstractMockitoTest;

class QueryConverterTest extends AbstractMockitoTest {
  @Mock private QueryRsqlVisitor rsqlVisitor;
  private QueryConverter queryConverter;

  @BeforeEach
  void setUpBeforeMethod() {
    // RSQLParser is final, can't be mocked
    queryConverter = new QueryConverter(new RSQLParser(), rsqlVisitor);
  }

  @Test
  void testRsqlConverter() {
    assertThrows(NullPointerException.class, () -> new QueryConverter(null, null));
  }

  @Test
  void testCreateQuery() {
    String rsqlQuery = "item==value";
    ComparisonNode node =
        new ComparisonNode(new ComparisonOperator("=="), "item", singletonList("value"));
    Query query = Query.builder().setItem("item").setOperator(Operator.EQUALS).build();
    when(node.accept(rsqlVisitor)).thenReturn(query);
    assertEquals(query, queryConverter.convert(rsqlQuery));
    verify(rsqlVisitor).visit(node, null);
  }

  @Test
  void testCreateQueryParseException() {
    String rsqlQuery = "illegalQuery";
    assertThrows(QueryParseException.class, () -> queryConverter.convert(rsqlQuery));
  }

  @Test(
      expectedExceptions = UnknownQueryOperatorException.class,
      expectedExceptionsMessageRegExp = "operator:=illegalOperator=")
  public void testCreateQueryUnknownQueryOperatorException() {
    String rsqlQuery = "item=illegalOperator=value";
    queryConverter.convert(rsqlQuery);
  }
}

package org.molgenis.web.rsql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.web.exception.UnsupportedRsqlOperationException;
import org.molgenis.web.exception.UnsupportedRsqlOperatorException;

@ExtendWith(MockitoExtension.class)
class MolgenisRSQLVisitorTest {

  private MolgenisRSQLVisitor visitor;

  @Mock Repository repository;
  @Mock EntityType entityType;
  @Mock Attribute attribute;
  @Mock RSQLValueParser valueParser;

  @BeforeEach
  void beforeEach() {
    visitor = new MolgenisRSQLVisitor(repository, valueParser);
  }

  @Test
  void testVisitComparisonNodeFieldSearchQuery() {
    ComparisonOperator searchQuery = new ComparisonOperator("=sq=");
    ComparisonNode node = new ComparisonNode(searchQuery, "version", List.of("8.3.*"));

    Query actual = visitor.visit(node);

    Query expected = new QueryImpl().searchQuery("version", "8.3.*");
    assertEquals(expected, actual);
  }

  @Test
  void testVisitComparisonNodeAllFieldsSearchQuery() {
    ComparisonOperator searchQuery = new ComparisonOperator("=sq=");
    ComparisonNode node = new ComparisonNode(searchQuery, "*", List.of("8.3.*"));

    Query actual = visitor.visit(node);

    Query expected = new QueryImpl().searchQuery("8.3.*");
    assertEquals(expected, actual);
  }

  @Test
  void testVisitUnknownOperator() {
    ComparisonOperator unknown = new ComparisonOperator("=foo=");
    ComparisonNode node = new ComparisonNode(unknown, "*", List.of("8.3.*"));

    assertThrows(IllegalStateException.class, () -> visitor.visit(node));
  }

  @Test
  void testVisitUnsupportedOperator() {
    ComparisonOperator unsupported = new ComparisonOperator("=dismax=");
    ComparisonNode node = new ComparisonNode(unsupported, "*", List.of("8.3.*"));

    assertThrows(UnsupportedRsqlOperatorException.class, () -> visitor.visit(node));
  }

  @Test
  void testVisitUnsupportedOperation() {
    when(repository.getEntityType()).thenReturn(entityType);
    when(entityType.getAttribute("name")).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(STRING);
    ComparisonOperator ge = new ComparisonOperator("=ge=");
    ComparisonNode node = new ComparisonNode(ge, "name", List.of("bar"));

    assertThrows(UnsupportedRsqlOperationException.class, () -> visitor.visit(node));
  }
}

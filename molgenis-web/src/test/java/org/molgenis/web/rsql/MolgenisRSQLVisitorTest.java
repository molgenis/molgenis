package org.molgenis.web.rsql;

import static org.junit.jupiter.api.Assertions.*;

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
import org.molgenis.data.support.QueryImpl;

@ExtendWith(MockitoExtension.class)
class MolgenisRSQLVisitorTest {

  private MolgenisRSQLVisitor visitor;

  @Mock Repository repository;
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
}

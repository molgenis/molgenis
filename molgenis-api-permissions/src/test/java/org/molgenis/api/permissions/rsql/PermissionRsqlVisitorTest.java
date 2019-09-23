package org.molgenis.api.permissions.rsql;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.api.permissions.exceptions.rsql.UnsupportedPermissionQueryOperatorException;

class PermissionRsqlVisitorTest {

  private PermissionRsqlVisitor visitor;

  @BeforeEach
  void setUp() {
    visitor = new PermissionRsqlVisitor();
  }

  @Test
  void testVisitAnd() {
    Node node = mock(Node.class);
    AndNode andNode = new AndNode(Arrays.asList(node, node));
    assertThrows(UnsupportedPermissionQueryOperatorException.class, () -> visitor.visit(andNode));
  }

  @Test
  void testVisit() {
    ComparisonNode comparisonNode =
        new ComparisonNode(
            new ComparisonOperator("=="), "user", Collections.singletonList("user1"));
    assertEquals(new PermissionsQuery(asList("user1"), emptyList()), visitor.visit(comparisonNode));
  }

  @Test
  void testVisit2() {
    ComparisonNode comparisonNode =
        new ComparisonNode(
            new ComparisonOperator("=in=", true), "role", Arrays.asList("role1", "role2"));
    assertEquals(
        new PermissionsQuery(emptyList(), asList("role1", "role2")), visitor.visit(comparisonNode));
  }

  @Test
  void testVisitOr() {
    ComparisonNode comparisonNodeUser =
        new ComparisonNode(
            new ComparisonOperator("=="), "user", Collections.singletonList("user1"));
    ComparisonNode comparisonNodeRole =
        new ComparisonNode(
            new ComparisonOperator("=in=", true), "role", Arrays.asList("role1", "role2"));
    OrNode orNode = new OrNode(Arrays.asList(comparisonNodeUser, comparisonNodeRole));
    assertEquals(
        new PermissionsQuery(asList("user1"), asList("role1", "role2")), visitor.visit(orNode));
  }
}

package org.molgenis.api.permissions.rsql;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import java.util.List;
import org.molgenis.api.permissions.exceptions.rsql.UnknownPermissionQueryParamException;
import org.molgenis.api.permissions.exceptions.rsql.UnsupportedPermissionQueryOperatorException;

public class PermissionRsqlVisitor extends NoArgRSQLVisitorAdapter<PermissionsQuery> {

  @Override
  public PermissionsQuery visit(AndNode andNode) {
    throw new UnsupportedPermissionQueryOperatorException();
  }

  @Override
  public PermissionsQuery visit(OrNode orNode) {
    List<Node> nodes = orNode.getChildren();
    PermissionsQuery permissionsQuery = new PermissionsQuery();
    if (nodes.size() == 2) {
      for (Node node : nodes) {
        if (node instanceof ComparisonNode) {
          setQuery((ComparisonNode) node, permissionsQuery);
        } else {
          throw new UnsupportedOperationException("invalide node");
        }
      }
    }
    return permissionsQuery;
  }

  @Override
  public PermissionsQuery visit(ComparisonNode comparisonNode) {
    return setQuery(comparisonNode, new PermissionsQuery());
  }

  private PermissionsQuery setQuery(
      ComparisonNode comparisonNode, PermissionsQuery permissionsQuery) {
    String key = comparisonNode.getSelector();
    List<String> arguments = comparisonNode.getArguments();
    if (key.equalsIgnoreCase("user")) {
      permissionsQuery.setUsers(arguments);
    } else if (key.equalsIgnoreCase("role")) {
      permissionsQuery.setRoles(arguments);
    } else {
      throw new UnknownPermissionQueryParamException(key);
    }
    return permissionsQuery;
  }
}

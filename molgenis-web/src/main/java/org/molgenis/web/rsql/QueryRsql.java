package org.molgenis.web.rsql;

import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;

/**
 * Create MOLGENIS Query from RSQL node based on entity meta data.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
public class QueryRsql {
  private final Node rootNode;

  public QueryRsql(Node rootNode) {
    this.rootNode = rootNode;
  }

  public Query<Entity> createQuery(Repository<Entity> repository) {
    MolgenisRSQLVisitor rsqlVisitor = new MolgenisRSQLVisitor(repository);
    return rootNode.accept(rsqlVisitor);
  }
}

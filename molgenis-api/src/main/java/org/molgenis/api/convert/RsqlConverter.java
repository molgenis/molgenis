package org.molgenis.api.convert;

import static java.util.Objects.requireNonNull;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.api.model.Query;
import org.springframework.stereotype.Service;

/**
 * Rsql/fiql parser
 *
 * <p>Creates a Query object from a rsql/fiql query string.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
@Service
public class RsqlConverter {
  private final RSQLParser rsqlParser;

  public RsqlConverter(RSQLParser rsqlParser) {
    this.rsqlParser = requireNonNull(rsqlParser);
  }

  public Query createQuery(String rsql) {
    Node rootNode = rsqlParser.parse(rsql);
    RsqlVisitor visitor = new RsqlVisitor();

    return rootNode.accept(visitor);
  }
}

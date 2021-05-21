package org.molgenis.web.rsql;

import static java.util.Objects.requireNonNull;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.UnknownOperatorException;
import org.molgenis.api.convert.QueryParseException;
import org.molgenis.api.convert.UnknownQueryOperatorException;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.springframework.stereotype.Service;

/**
 * Rsql/fiql parser
 *
 * <p>Creates a Query object from a rsql/fiql query string.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
@Service
public class MolgenisRSQL {
  private final RSQLParser rsqlParser;

  public MolgenisRSQL(RSQLParser rsqlParser) {
    this.rsqlParser = requireNonNull(rsqlParser);
  }

  public Query<Entity> createQuery(String rsql, Repository<Entity> repository) {
    try {
      var rootNode = rsqlParser.parse(rsql);
      var visitor = new MolgenisRSQLVisitor(repository);
      return rootNode.accept(visitor);
    } catch (RSQLParserException ex) {
      var cause = ex.getCause();
      if (cause instanceof UnknownOperatorException) {
        throw new UnknownQueryOperatorException(((UnknownOperatorException) cause).getOperator());
      }
      throw new QueryParseException(ex);
    }
  }
}

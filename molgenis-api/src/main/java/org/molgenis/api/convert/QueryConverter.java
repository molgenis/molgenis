package org.molgenis.api.convert;

import static java.util.Objects.requireNonNull;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.UnknownOperatorException;
import cz.jirutka.rsql.parser.ast.Node;
import javax.annotation.Nonnull;
import org.molgenis.api.model.Query;
import org.springframework.core.convert.converter.Converter;

public class QueryConverter implements Converter<String, Query> {
  private final RSQLParser rsqlParser;
  private final QueryRsqlVisitor rsqlVisitor;

  public QueryConverter(RSQLParser rsqlParser, QueryRsqlVisitor rsqlVisitor) {
    this.rsqlParser = requireNonNull(rsqlParser);
    this.rsqlVisitor = requireNonNull(rsqlVisitor);
  }

  @Override
  public Query convert(@Nonnull String source) {
    Node node;
    try {
      node = rsqlParser.parse(source);
    } catch (RSQLParserException e) {
      Throwable cause = e.getCause();
      if (cause instanceof UnknownOperatorException) {
        String operator = ((UnknownOperatorException) cause).getOperator();
        throw new UnknownQueryOperatorException(operator);
      } else {
        throw new QueryParseException(e);
      }
    }
    return node.accept(rsqlVisitor);
  }
}

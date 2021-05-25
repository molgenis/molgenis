package org.molgenis.web.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.UnknownOperatorException;
import org.molgenis.api.convert.QueryParseException;
import org.molgenis.api.convert.UnknownQueryOperatorException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class QueryRsqlConverter implements Converter<String, QueryRsql> {
  private final RSQLParser rsqlParser;

  public QueryRsqlConverter(RSQLParser rsqlParser) {
    this.rsqlParser = rsqlParser;
  }

  @Override
  public QueryRsql convert(@NonNull String source) {
    try {
      var rootNode = rsqlParser.parse(source);
      return new QueryRsql(rootNode);
    } catch (RSQLParserException ex) {
      var cause = ex.getCause();
      if (cause instanceof UnknownOperatorException) {
        throw new UnknownQueryOperatorException(((UnknownOperatorException) cause).getOperator());
      }
      throw new QueryParseException(ex);
    }
  }
}

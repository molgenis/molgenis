package org.molgenis.api.convert;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import org.molgenis.api.model.Query;
import org.springframework.core.convert.converter.Converter;

public class QueryConverter implements Converter<String, Query> {

  private final RSQLParser rsqlParser;

  public QueryConverter(RSQLParser rsqlParser) {
    this.rsqlParser = rsqlParser;
  }

  @Override
  public Query convert(String source) {
    try {
      RsqlConverter rsql = new RsqlConverter(rsqlParser);
      return rsql.createQuery(source);
    } catch (RSQLParserException e) {
      throw new RuntimeException(e); // FIXME: Coded
    }
  }
}

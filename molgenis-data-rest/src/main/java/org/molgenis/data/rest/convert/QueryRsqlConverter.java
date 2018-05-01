package org.molgenis.data.rest.convert;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.web.rsql.QueryRsql;
import org.springframework.core.convert.converter.Converter;

public class QueryRsqlConverter implements Converter<String, QueryRsql>
{
	private final RSQLParser rsqlParser;

	public QueryRsqlConverter(RSQLParser rsqlParser)
	{
		this.rsqlParser = rsqlParser;
	}

	@Override
	public QueryRsql convert(String source)
	{
		Node rootNode = rsqlParser.parse(source);
		return new QueryRsql(rootNode);
	}
}

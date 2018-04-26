package org.molgenis.data.rest.convert;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.core.ui.data.rsql.AggregateQueryRsql;
import org.springframework.core.convert.converter.Converter;

import static com.google.common.base.Preconditions.checkNotNull;

public class AggregateQueryRsqlConverter implements Converter<String, AggregateQueryRsql>
{
	private final RSQLParser rsqlParser;

	public AggregateQueryRsqlConverter(RSQLParser rsqlParser)
	{
		this.rsqlParser = checkNotNull(rsqlParser);
	}

	@Override
	public AggregateQueryRsql convert(String source)
	{
		Node rootNode = rsqlParser.parse(source);
		return new AggregateQueryRsql(rootNode);
	}
}

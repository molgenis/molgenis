package org.molgenis.data.rsql;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.springframework.stereotype.Service;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.Node;

/**
 * Rsql/fiql parser
 * 
 * Creates a Query object from a rsql/fiql query string.
 * 
 * @see https://github.com/jirutka/rsql-parser
 */
@Service
public class MolgenisRSQL
{
	private final RSQLParser rsqlParser = new RSQLParser();

	public Query createQuery(String rsql, EntityMetaData entityMetaData) throws RSQLParserException
	{
		Node rootNode = rsqlParser.parse(rsql);
		MolgenisRSQLVisitor visitor = new MolgenisRSQLVisitor(entityMetaData);

		return rootNode.accept(visitor);
	}
}

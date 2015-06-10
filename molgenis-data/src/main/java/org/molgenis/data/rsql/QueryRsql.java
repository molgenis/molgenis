package org.molgenis.data.rsql;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;

import cz.jirutka.rsql.parser.ast.Node;

/**
 * Create MOLGENIS Query from RSQL node based on entity meta data.
 * 
 * @see https://github.com/jirutka/rsql-parser
 */
public class QueryRsql
{
	private final Node rootNode;

	public QueryRsql(Node rootNode)
	{
		this.rootNode = rootNode;
	}

	public Query createQuery(EntityMetaData entityMetaData)
	{
		MolgenisRSQLVisitor rsqlVisitor = new MolgenisRSQLVisitor(entityMetaData);
		return rootNode.accept(rsqlVisitor);
	}
}

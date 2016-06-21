package org.molgenis.data.rsql;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.EntityMetaData;

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

	public Query<Entity> createQuery(EntityMetaData entityMetaData)
	{
		MolgenisRSQLVisitor rsqlVisitor = new MolgenisRSQLVisitor(entityMetaData);
		return rootNode.accept(rsqlVisitor);
	}
}

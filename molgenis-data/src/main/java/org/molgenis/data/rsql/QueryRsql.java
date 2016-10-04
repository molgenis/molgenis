package org.molgenis.data.rsql;

import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityMetaData;

/**
 * Create MOLGENIS Query from RSQL node based on entity meta data.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
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

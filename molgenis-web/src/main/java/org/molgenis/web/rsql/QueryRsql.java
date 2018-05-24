package org.molgenis.web.rsql;

import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;

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

	public Query<Entity> createQuery(EntityType entityType)
	{
		MolgenisRSQLVisitor rsqlVisitor = new MolgenisRSQLVisitor(entityType);
		return rootNode.accept(rsqlVisitor);
	}
}

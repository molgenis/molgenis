package org.molgenis.data.rsql;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;

import cz.jirutka.rsql.parser.ast.Node;

/**
 * Create MOLGENIS aggregation query from RSQL node.
 * 
 * @see https://github.com/jirutka/rsql-parser
 */
public class AggregateQueryRsql
{
	private final Node rootNode;

	public AggregateQueryRsql(Node rootNode)
	{
		this.rootNode = checkNotNull(rootNode);
	}

	public AggregateQuery createAggregateQuery(EntityMetaData entityMetaData, Query query)
	{
		AggregateQueryRsqlVisitor rsqlVisitor = new AggregateQueryRsqlVisitor(entityMetaData, query);
		return rootNode.accept(rsqlVisitor);
	}
}

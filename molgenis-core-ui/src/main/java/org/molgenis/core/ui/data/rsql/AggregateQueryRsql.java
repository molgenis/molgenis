package org.molgenis.core.ui.data.rsql;

import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.meta.model.EntityType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Create MOLGENIS aggregation query from RSQL node.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
public class AggregateQueryRsql
{
	private final Node rootNode;

	public AggregateQueryRsql(Node rootNode)
	{
		this.rootNode = checkNotNull(rootNode);
	}

	public AggregateQuery createAggregateQuery(EntityType entityType, Query<Entity> query)
	{
		AggregateQueryRsqlVisitor rsqlVisitor = new AggregateQueryRsqlVisitor(entityType, query);
		return rootNode.accept(rsqlVisitor);
	}
}

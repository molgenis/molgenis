package org.molgenis.web.rsql;

import cz.jirutka.rsql.parser.ast.*;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.AggregateQueryImpl;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * RSQLVisitor implementation for molgenis Query.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
public class AggregateQueryRsqlVisitor extends NoArgRSQLVisitorAdapter<AggregateQuery>
{
	private final EntityType entityType;
	private final AggregateQueryImpl aggsQ;

	public AggregateQueryRsqlVisitor(EntityType entityType, Query<Entity> query)
	{
		this.entityType = checkNotNull(entityType);
		this.aggsQ = new AggregateQueryImpl().query(query);
	}

	@Override
	public AggregateQuery visit(AndNode node)
	{
		for (Node child : node)
		{
			child.accept(this);
		}

		return aggsQ;
	}

	@Override
	public AggregateQuery visit(OrNode node)
	{
		throw new MolgenisQueryException("RSQL query operator OR (';' or 'or') not allowed in aggregates query");
	}

	@Override
	public AggregateQuery visit(ComparisonNode node)
	{
		String symbol = node.getOperator().getSymbol();
		if (!symbol.equals("=="))
		{
			throw new MolgenisQueryException(
					String.format("RSQL query symbol [%s] not allowed in aggregates query, use ['==']", symbol));
		}

		String selector = node.getSelector();
		switch (selector)
		{
			case "x":
				aggsQ.setAttributeX(getAttribute(node));
				break;
			case "y":
				aggsQ.setAttributeY(getAttribute(node));
				break;
			case "distinct":
				aggsQ.setAttributeDistinct(getAttribute(node));
				break;
			default:
				throw new MolgenisQueryException(String.format(
						"RSQL query selector [%s] not allowed in aggregates query, use ['x', 'y' or 'distinct']",
						selector));
		}
		return aggsQ;
	}

	private Attribute getAttribute(ComparisonNode node)
	{
		List<String> args = node.getArguments();
		if (args.size() != 1)
		{
			throw new MolgenisQueryException(
					String.format("RSQL query value must have exactly one value instead of [%s]",
							StringUtils.join(args, ',')));
		}
		String attrName = args.iterator().next();

		String[] attrTokens = attrName.split("\\.");
		Attribute attr = entityType.getAttribute(attrTokens[0]);
		if (attr == null)
		{
			throw new UnknownAttributeException(entityType, attrName);
		}
		EntityType entityTypeAtDepth;
		for (int i = 1; i < attrTokens.length; ++i)
		{
			entityTypeAtDepth = attr.getRefEntity();
			attr = entityTypeAtDepth.getAttribute(attrTokens[i]);
			if (attr == null)
			{
				throw new UnknownAttributeException(entityTypeAtDepth, attrName);
			}
		}

		return attr;
	}
}

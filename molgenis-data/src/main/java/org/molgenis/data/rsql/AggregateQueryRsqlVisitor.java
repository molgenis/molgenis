package org.molgenis.data.rsql;

import cz.jirutka.rsql.parser.ast.*;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.AggregateQueryImpl;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * RSQLVisitor implementation for molgenis Query.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
public class AggregateQueryRsqlVisitor extends NoArgRSQLVisitorAdapter<AggregateQuery>
{
	private final EntityMetaData entityMetaData;
	private final AggregateQueryImpl aggsQ;

	public AggregateQueryRsqlVisitor(EntityMetaData entityMetaData, Query<Entity> query)
	{
		this.entityMetaData = checkNotNull(entityMetaData);
		this.aggsQ = new AggregateQueryImpl().query(query);
	}

	@Override
	public AggregateQuery visit(AndNode node)
	{
		for (Iterator<Node> it = node.iterator(); it.hasNext(); )
		{
			Node child = it.next();
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

	private AttributeMetaData getAttribute(ComparisonNode node)
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
		AttributeMetaData attr = entityMetaData.getAttribute(attrTokens[0]);
		if (attr == null)
		{
			throw new UnknownAttributeException("Unknown attribute [" + attrName + "]");
		}
		EntityMetaData entityMetaDataAtDepth = entityMetaData;
		for (int i = 1; i < attrTokens.length; ++i)
		{
			entityMetaDataAtDepth = attr.getRefEntity();
			attr = entityMetaDataAtDepth.getAttribute(attrTokens[i]);
			if (attr == null)
			{
				throw new UnknownAttributeException("Unknown attribute [" + attrName + "]");
			}
		}

		return attr;
	}
}

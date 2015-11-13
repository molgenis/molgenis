package org.molgenis.data.rsql;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisQueryException;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.support.AggregateQueryImpl;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;

/**
 * RSQLVisitor implementation for molgenis Query.
 * 
 * @see https://github.com/jirutka/rsql-parser
 */
public class AggregateQueryRsqlVisitor extends NoArgRSQLVisitorAdapter<AggregateQuery>
{
	private final EntityMetaData entityMetaData;
	private final AggregateQueryImpl aggsQ;

	public AggregateQueryRsqlVisitor(EntityMetaData entityMetaData, Query query)
	{
		this.entityMetaData = checkNotNull(entityMetaData);
		this.aggsQ = new AggregateQueryImpl().query(query);
	}

	@Override
	public AggregateQuery visit(AndNode node)
	{
		for (Iterator<Node> it = node.iterator(); it.hasNext();)
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
			throw new MolgenisQueryException(String.format(
					"RSQL query value must have exactly one value instead of [%s]", StringUtils.join(args, ',')));
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

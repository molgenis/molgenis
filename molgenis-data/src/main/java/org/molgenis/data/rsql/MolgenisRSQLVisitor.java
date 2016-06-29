package org.molgenis.data.rsql;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import cz.jirutka.rsql.parser.ast.*;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;

import java.util.Iterator;
import java.util.List;

/**
 * RSQLVisitor implementation for molgenis Query.
 * 
 * @see https://github.com/jirutka/rsql-parser
 */
public class MolgenisRSQLVisitor extends NoArgRSQLVisitorAdapter<Query<Entity>>
{
	private final QueryImpl<Entity> q = new QueryImpl<Entity>();
	private final EntityMetaData entityMetaData;

	public MolgenisRSQLVisitor(EntityMetaData entityMetaData)
	{
		this.entityMetaData = entityMetaData;
	}

	@Override
	public Query<Entity> visit(AndNode node)
	{
		q.nest(); // TODO only nest if more than one child

		for (Iterator<Node> it = node.iterator(); it.hasNext();)
		{
			Node child = it.next();
			child.accept(this);

			if (it.hasNext())
			{
				q.and();
			}
		}

		q.unnest();

		return q;
	}

	@Override
	public Query<Entity> visit(OrNode node)
	{
		q.nest(); // TODO only nest if more than one child

		for (Iterator<Node> it = node.iterator(); it.hasNext();)
		{
			Node child = it.next();
			child.accept(this);

			if (it.hasNext())
			{
				q.or();
			}
		}

		q.unnest();

		return q;
	}

	@Override
	public Query<Entity> visit(ComparisonNode node)
	{
		String attrName = node.getSelector();
		String symbol = node.getOperator().getSymbol();
		List<String> values = node.getArguments();
		switch (symbol)
		{	case "=notlike=":
				String notLikeValue = values.get(0);
				q.not().like(attrName, notLikeValue);
				break;
			case "=q=":
				String searchValue = values.get(0);
				if (attrName.equals("*"))
				{
					q.search(searchValue);
				}
				else
				{
					q.search(attrName, searchValue);
				}
				break;
			case "==":
				Object eqValue = DataConverter.convert(values.get(0), getAttribute(node));
				q.eq(attrName, eqValue);
				break;
			case "=in=":
				AttributeMetaData inAttr = getAttribute(node);
				q.in(attrName, Iterables.transform(values, new Function<String, Object>()
				{
					@Override
					public Object apply(String value)
					{
						return DataConverter.convert(value, inAttr);
					}
				}));
				break;
			case "=lt=":
			case "<":
				AttributeMetaData ltAttr = getAttribute(node);
				validateNumericOrDate(ltAttr, symbol);
				Object ltValue = DataConverter.convert(values.get(0), ltAttr);
				q.lt(attrName, ltValue);
				break;
			case "=le=":
			case "<=":
				AttributeMetaData leAttr = getAttribute(node);
				validateNumericOrDate(leAttr, symbol);
				Object leValue = DataConverter.convert(values.get(0), leAttr);
				q.le(attrName, leValue);
				break;
			case "=gt=":
			case ">":
				AttributeMetaData gtAttr = getAttribute(node);
				validateNumericOrDate(gtAttr, symbol);
				Object gtValue = DataConverter.convert(values.get(0), gtAttr);
				q.gt(attrName, gtValue);
				break;
			case "=ge=":
			case ">=":
				AttributeMetaData geAttr = getAttribute(node);
				validateNumericOrDate(geAttr, symbol);
				Object geValue = DataConverter.convert(values.get(0), geAttr);
				q.ge(attrName, geValue);
				break;
			case "=rng=":
				AttributeMetaData rngAttr = getAttribute(node);
				validateNumericOrDate(rngAttr, symbol);
				Object fromValue = values.get(0) != null ? DataConverter.convert(values.get(0), rngAttr) : null;
				Object toValue = values.get(1) != null ? DataConverter.convert(values.get(1), rngAttr) : null;
				q.rng(attrName, fromValue, toValue);
				break;
			case "=like=":
				String likeValue = values.get(0);
				q.like(attrName, likeValue);
				break;
			case "!=":
				Object notEqValue = DataConverter.convert(values.get(0), getAttribute(node));
				q.not().eq(attrName, notEqValue);
				break;
			case "=should=":
				throw new MolgenisQueryException("Unsupported RSQL query operator [" + symbol + "]");
			case "=dismax=":
				throw new MolgenisQueryException("Unsupported RSQL query operator [" + symbol + "]");
			case "=fuzzy=":
				throw new MolgenisQueryException("Unsupported RSQL query operator [" + symbol + "]");
			default:
				throw new MolgenisQueryException("Unknown RSQL query operator [" + symbol + "]");
		}
		return q;
	}

	private void validateNumericOrDate(AttributeMetaData attr, String symbol)
	{
		switch (attr.getDataType())
		{
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case INT:
			case LONG:
				break;
			// $CASES-OMITTED$
			default:
				throw new IllegalArgumentException("Can't perform operator '\" + symbol + \"' on attribute '\""
						+ attr.getName() + "\"");
		}
	}

	private AttributeMetaData getAttribute(ComparisonNode node)
	{
		String attrName = node.getSelector();

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

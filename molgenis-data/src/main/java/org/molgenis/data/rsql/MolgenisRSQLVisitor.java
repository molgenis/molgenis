package org.molgenis.data.rsql;

import cz.jirutka.rsql.parser.ast.*;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.QueryImpl;

import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * RSQLVisitor implementation that creates {@link Query} objects for an RSQL tree.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
public class MolgenisRSQLVisitor extends NoArgRSQLVisitorAdapter<Query<Entity>>
{
	private final QueryImpl<Entity> q = new QueryImpl<>();
	private final EntityMetaData entityMetaData;
	private final RSQLValueParser rsqlValueParser = new RSQLValueParser();

	public MolgenisRSQLVisitor(EntityMetaData entityMetaData)
	{
		this.entityMetaData = entityMetaData;
	}

	@Override
	public Query<Entity> visit(AndNode node)
	{
		q.nest(); // TODO only nest if more than one child

		for (Iterator<Node> it = node.iterator(); it.hasNext(); )
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

		for (Iterator<Node> it = node.iterator(); it.hasNext(); )
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
		{
			case "=notlike=":
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
				Object eqValue = rsqlValueParser.parse(values.get(0), getAttribute(node));
				q.eq(attrName, eqValue);
				break;
			case "=in=":
				AttributeMetaData inAttr = getAttribute(node);
				q.in(attrName, values.stream().map(value -> rsqlValueParser.parse(value, inAttr)).collect(toList()));
				break;
			case "=lt=":
			case "<":
				AttributeMetaData ltAttr = getAttribute(node);
				validateNumericOrDate(ltAttr);
				Object ltValue = rsqlValueParser.parse(values.get(0), ltAttr);
				q.lt(attrName, ltValue);
				break;
			case "=le=":
			case "<=":
				AttributeMetaData leAttr = getAttribute(node);
				validateNumericOrDate(leAttr);
				Object leValue = rsqlValueParser.parse(values.get(0), leAttr);
				q.le(attrName, leValue);
				break;
			case "=gt=":
			case ">":
				AttributeMetaData gtAttr = getAttribute(node);
				validateNumericOrDate(gtAttr);
				Object gtValue = rsqlValueParser.parse(values.get(0), gtAttr);
				q.gt(attrName, gtValue);
				break;
			case "=ge=":
			case ">=":
				AttributeMetaData geAttr = getAttribute(node);
				validateNumericOrDate(geAttr);
				Object geValue = rsqlValueParser.parse(values.get(0), geAttr);
				q.ge(attrName, geValue);
				break;
			case "=rng=":
				AttributeMetaData rngAttr = getAttribute(node);
				validateNumericOrDate(rngAttr);
				Object fromValue = values.get(0) != null ? rsqlValueParser.parse(values.get(0), rngAttr) : null;
				Object toValue = values.get(1) != null ? rsqlValueParser.parse(values.get(1), rngAttr) : null;
				q.rng(attrName, fromValue, toValue);
				break;
			case "=like=":
				String likeValue = values.get(0);
				q.like(attrName, likeValue);
				break;
			case "!=":
				Object notEqValue = rsqlValueParser.parse(values.get(0), getAttribute(node));
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

	private void validateNumericOrDate(AttributeMetaData attr)
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
				throw new IllegalArgumentException(
						"Can't perform operator '\" + symbol + \"' on attribute '\"" + attr.getName() + "\"");
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
		EntityMetaData entityMetaDataAtDepth;
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

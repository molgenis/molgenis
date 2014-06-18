package org.molgenis.data.rsql;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE_TIME;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.INT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.LONG;

import java.util.Iterator;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.support.QueryImpl;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.EqualNode;
import cz.jirutka.rsql.parser.ast.GreaterThanNode;
import cz.jirutka.rsql.parser.ast.GreaterThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.InNode;
import cz.jirutka.rsql.parser.ast.LessThanNode;
import cz.jirutka.rsql.parser.ast.LessThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.NotEqualNode;
import cz.jirutka.rsql.parser.ast.NotInNode;
import cz.jirutka.rsql.parser.ast.OrNode;

/**
 * RSQLVisitor implementation for molgenis Query.
 * 
 * @see https://github.com/jirutka/rsql-parser
 */
public class MolgenisRSQLVisitor extends NoArgRSQLVisitorAdapter<Query>
{
	private final QueryImpl q = new QueryImpl();
	private final EntityMetaData entityMetaData;

	public MolgenisRSQLVisitor(EntityMetaData entityMetaData)
	{
		this.entityMetaData = entityMetaData;
	}

	@Override
	public Query visit(AndNode node)
	{
		q.nest();

		Iterator<Node> it = node.iterator();
		while (it.hasNext())
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
	public Query visit(OrNode node)
	{
		q.nest();

		Iterator<Node> it = node.iterator();
		while (it.hasNext())
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
	public Query visit(EqualNode node)
	{
		AttributeMetaData attr = getAttribute(node);
		Object value = DataConverter.convert(node.getArguments().get(0), attr);

		return q.eq(attr.getName(), value);
	}

	@Override
	public Query visit(GreaterThanOrEqualNode node)
	{
		return numericalQueryRule(node, Operator.GREATER_EQUAL);
	}

	@Override
	public Query visit(GreaterThanNode node)
	{
		return numericalQueryRule(node, Operator.GREATER);
	}

	@Override
	public Query visit(LessThanOrEqualNode node)
	{
		return numericalQueryRule(node, Operator.LESS_EQUAL);
	}

	@Override
	public Query visit(LessThanNode node)
	{
		return numericalQueryRule(node, Operator.LESS);
	}

	@Override
	public Query visit(NotEqualNode node)
	{
		throw new UnsupportedOperationException("!= not supported");
	}

	@Override
	public Query visit(NotInNode node)
	{
		throw new UnsupportedOperationException("=out= not supported");
	}

	@Override
	public Query visit(InNode node)
	{
		// IN is not implemented ES
		throw new UnsupportedOperationException("=in= not supported");
	}

	private AttributeMetaData getAttribute(ComparisonNode node)
	{
		String attrName = node.getSelector();
		AttributeMetaData attr = entityMetaData.getAttribute(attrName);
		if (attr == null)
		{
			throw new UnknownAttributeException("Unknown attribute [" + attrName + "]");
		}

		return attr;
	}

	private Query numericalQueryRule(ComparisonNode node, Operator operator)
	{
		AttributeMetaData attr = getAttribute(node);
		FieldTypeEnum fieldType = attr.getDataType().getEnumType();

		if ((fieldType != INT) && (fieldType != LONG) && (fieldType != DECIMAL) && (fieldType != DATE)
				&& (fieldType != DATE_TIME))
		{
			throw new IllegalArgumentException("Can't perform operator '" + operator + "' on attribute '"
					+ attr.getName() + "'");
		}

		Object value = DataConverter.convert(node.getArguments().get(0), attr);

		q.addRule(new QueryRule(attr.getName(), operator, value));

		return q;
	}
}

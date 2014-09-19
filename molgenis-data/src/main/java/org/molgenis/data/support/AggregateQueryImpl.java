package org.molgenis.data.support;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Query;

public class AggregateQueryImpl implements AggregateQuery
{
	private AttributeMetaData attrX;
	private AttributeMetaData attrY;
	private AttributeMetaData attrDistinct;
	private Query q;

	public AggregateQueryImpl()
	{
	}

	public AggregateQueryImpl(AttributeMetaData attrX, AttributeMetaData attrY, AttributeMetaData attrDistinct, Query q)
	{
		this.attrX = attrX;
		this.attrY = attrY;
		this.attrDistinct = attrDistinct;
		this.q = q;
	}

	public AggregateQueryImpl attrX(AttributeMetaData attrX)
	{
		this.attrX = attrX;
		return this;
	}

	public AggregateQueryImpl attrY(AttributeMetaData attrY)
	{
		this.attrY = attrY;
		return this;
	}

	public AggregateQueryImpl attrDistinct(AttributeMetaData attrDistinct)
	{
		this.attrDistinct = attrDistinct;
		return this;
	}

	public AggregateQueryImpl query(Query q)
	{
		this.q = q;
		return this;
	}

	public void setAttributeX(AttributeMetaData attrX)
	{
		this.attrX = attrX;
	}

	public void setAttributeY(AttributeMetaData attrY)
	{
		this.attrY = attrY;
	}

	public void setAttributeDistinct(AttributeMetaData attrDistinct)
	{
		this.attrDistinct = attrDistinct;
	}

	public void setQuery(Query q)
	{
		this.q = q;
	}

	@Override
	public Query getQuery()
	{
		return q;
	}

	@Override
	public AttributeMetaData getAttributeX()
	{
		return attrX;
	}

	@Override
	public AttributeMetaData getAttributeY()
	{
		return attrY;
	}

	@Override
	public AttributeMetaData getAttributeDistinct()
	{
		return attrDistinct;
	}
}

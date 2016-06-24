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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attrDistinct == null) ? 0 : attrDistinct.hashCode());
		result = prime * result + ((attrX == null) ? 0 : attrX.hashCode());
		result = prime * result + ((attrY == null) ? 0 : attrY.hashCode());
		result = prime * result + ((q == null) ? 0 : q.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AggregateQueryImpl other = (AggregateQueryImpl) obj;
		if (attrDistinct == null)
		{
			if (other.attrDistinct != null) return false;
		}
		else if (!attrDistinct.equals(other.attrDistinct)) return false;
		if (attrX == null)
		{
			if (other.attrX != null) return false;
		}
		else if (!attrX.equals(other.attrX)) return false;
		if (attrY == null)
		{
			if (other.attrY != null) return false;
		}
		else if (!attrY.equals(other.attrY)) return false;
		if (q == null)
		{
			if (other.q != null) return false;
		}
		else if (!q.equals(other.q)) return false;
		return true;
	}
}

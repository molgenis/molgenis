package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.meta.model.Attribute;

public class AggregateQueryImpl implements AggregateQuery
{
	private Attribute attrX;
	private Attribute attrY;
	private Attribute attrDistinct;
	private Query<Entity> q;

	public AggregateQueryImpl()
	{
	}

	public AggregateQueryImpl(Attribute attrX, Attribute attrY, Attribute attrDistinct, Query<Entity> q)
	{
		this.attrX = attrX;
		this.attrY = attrY;
		this.attrDistinct = attrDistinct;
		this.q = q;
	}

	public AggregateQueryImpl attrX(Attribute attrX)
	{
		this.attrX = attrX;
		return this;
	}

	public AggregateQueryImpl attrY(Attribute attrY)
	{
		this.attrY = attrY;
		return this;
	}

	public AggregateQueryImpl attrDistinct(Attribute attrDistinct)
	{
		this.attrDistinct = attrDistinct;
		return this;
	}

	public AggregateQueryImpl query(Query<Entity> q)
	{
		this.q = q;
		return this;
	}

	public void setAttributeX(Attribute attrX)
	{
		this.attrX = attrX;
	}

	public void setAttributeY(Attribute attrY)
	{
		this.attrY = attrY;
	}

	public void setAttributeDistinct(Attribute attrDistinct)
	{
		this.attrDistinct = attrDistinct;
	}

	public void setQuery(Query<Entity> q)
	{
		this.q = q;
	}

	@Override
	public Query<Entity> getQuery()
	{
		return q;
	}

	@Override
	public Attribute getAttributeX()
	{
		return attrX;
	}

	@Override
	public Attribute getAttributeY()
	{
		return attrY;
	}

	@Override
	public Attribute getAttributeDistinct()
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

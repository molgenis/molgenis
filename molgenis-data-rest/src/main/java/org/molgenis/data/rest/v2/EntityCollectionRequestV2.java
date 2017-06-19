package org.molgenis.data.rest.v2;

import org.molgenis.data.Sort;
import org.molgenis.data.rsql.AggregateQueryRsql;
import org.molgenis.data.rsql.QueryRsql;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

class EntityCollectionRequestV2
{
	public static final int MAX_ROWS = 10000;
	public static final int DEFAULT_ROW_COUNT = 100;
	private QueryRsql q;
	private AggregateQueryRsql aggs;
	private Sort sort;
	private AttributeFilter attrs;

	@Min(0)
	private int start = 0;
	@Min(0)
	@Max(MAX_ROWS)
	private int num = DEFAULT_ROW_COUNT;

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getNum()
	{
		return num;
	}

	public void setNum(int num)
	{
		this.num = num;
	}

	public QueryRsql getQ()
	{
		return q;
	}

	public void setQ(QueryRsql q)
	{
		this.q = q;
	}

	public Sort getSort()
	{
		return sort;
	}

	public void setSort(Sort sort)
	{
		this.sort = sort;
	}

	public AttributeFilter getAttrs()
	{
		return attrs;
	}

	public void setAttrs(AttributeFilter attrs)
	{
		this.attrs = attrs;
	}

	public AggregateQueryRsql getAggs()
	{
		return aggs;
	}

	public void setAggs(AggregateQueryRsql aggs)
	{
		this.aggs = aggs;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("EntityCollectionRequestV2 [q=")
			   .append(q)
			   .append(", aggs=")
			   .append(aggs)
			   .append(", sort=")
			   .append(sort)
			   .append(", attrs=")
			   .append(attrs)
			   .append(", start=")
			   .append(start)
			   .append(", num=")
			   .append(num)
			   .append("]");
		return builder.toString();
	}
}
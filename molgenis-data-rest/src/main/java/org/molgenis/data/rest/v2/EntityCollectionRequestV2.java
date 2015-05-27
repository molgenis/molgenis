package org.molgenis.data.rest.v2;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.molgenis.data.QueryRule;
import org.springframework.data.domain.Sort;

class EntityCollectionRequestV2
{
	public static final int MAX_ROWS = 10000;
	public static final int DEFAULT_ROW_COUNT = 100;
	private List<QueryRule> q;
	private Sort sort;
	private AttributeFilter attributes;

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

	public List<QueryRule> getQ()
	{
		return q;
	}

	public void setQ(List<QueryRule> q)
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

	public AttributeFilter getAttributes()
	{
		return attributes;
	}

	public void setAttributes(AttributeFilter attributes)
	{
		this.attributes = attributes;
	}
}
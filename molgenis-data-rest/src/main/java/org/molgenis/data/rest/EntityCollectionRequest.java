package org.molgenis.data.rest;

import org.molgenis.data.QueryRule;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

public class EntityCollectionRequest
{
	public static final int MAX_ROWS = 10000;
	public static final int DEFAULT_ROW_COUNT = 100;
	private List<QueryRule> q;
	@SuppressWarnings("deprecation")
	private SortV1 sort;
	private String[] attributes;
	private String[] expand;

	@Min(0)
	private int start = 0;

	@Min(0)
	@Max(value = MAX_ROWS, message = "No more than {value} rows can be requested")
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

	@SuppressWarnings("deprecation")
	public SortV1 getSort()
	{
		return sort;
	}

	@SuppressWarnings("deprecation")
	public void setSort(SortV1 sort)
	{
		this.sort = sort;
	}

	public String[] getAttributes()
	{
		return attributes;
	}

	public void setAttributes(String[] attributes)
	{
		this.attributes = attributes;
	}

	public String[] getExpand()
	{
		return expand;
	}

	public void setExpand(String[] expand)
	{
		this.expand = expand;
	}
}
package org.molgenis.framework.server;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.molgenis.framework.db.QueryRule;

public class EntityCollectionRequest
{
	private List<QueryRule> q;
	@Min(0)
	private int start = 0;
	@Min(0)
	@Max(100)
	private int num = 10;

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
}
package org.molgenis.data.rest;

import org.molgenis.data.Entity;

public class EntityPager
{
	private final int start;
	private final int num;
	private final Long total;
	private final Iterable<Entity> iterable;

	public EntityPager(int start, int num, Long total, Iterable<Entity> iterable)
	{
		this.start = start;
		this.num = num;
		this.total = total;
		this.iterable = iterable;
	}

	public int getStart()
	{
		return start;
	}

	public int getNum()
	{
		return num;
	}

	public Long getTotal()
	{
		return total;
	}

	public Integer getNextStart()
	{
		if (total == null) return this.start + this.num;

		if (this.start + this.num > this.total - 1) return null;
		else return this.start + this.num;
	}

	public Integer getPrevStart()
	{
		if (this.start == 0) return null;
		else return this.start - this.num >= 0 ? this.start - this.num : 0;
	}

	public Iterable<Entity> getIterable()
	{
		return iterable;
	}
}
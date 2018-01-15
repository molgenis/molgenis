package org.molgenis.data.util;

import org.molgenis.data.Entity;

public class EntityPager<T extends Entity>
{
	private final int start;
	private final int num;
	private final long total;
	private final Iterable<T> iterable;

	public EntityPager(int start, int num, long total, Iterable<T> iterable)
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

	public long getTotal()
	{
		return total;
	}

	public Integer getNextStart()
	{
		if (this.start + this.num > this.total - 1) return null;
		else return this.start + this.num;
	}

	public Integer getPrevStart()
	{
		if (this.start == 0) return null;
		else return this.start - this.num >= 0 ? this.start - this.num : 0;
	}

	public Iterable<T> getIterable()
	{
		return iterable;
	}
}
package org.molgenis.framework.server;

import java.util.List;

import org.molgenis.util.EntityPager;

public class EntityCollectionResponse<T>
{
	private final String href;
	private final int start;
	private final int num;
	private final int total;
	private final String prevHref;
	private final String nextHref;
	private final List<T> items;

	public EntityCollectionResponse(EntityPager<?> entityPager, List<T> items, String href)
	{
		this.href = href;
		this.start = entityPager.getStart();
		this.num = entityPager.getNum();
		this.total = entityPager.getTotal();
		Integer prevStart = entityPager.getPrevStart();
		this.prevHref = prevStart != null ? this.href + "?start=" + prevStart + "&num=" + this.num : null;
		Integer nextStart = entityPager.getNextStart();
		this.nextHref = nextStart != null ? this.href + "?start=" + nextStart + "&num=" + this.num : null;
		this.items = items;
	}

	public String getHref()
	{
		return href;
	}

	public int getStart()
	{
		return start;
	}

	public int getNum()
	{
		return num;
	}

	public int getTotal()
	{
		return total;
	}

	public String getPrevHref()
	{
		return prevHref;
	}

	public String getNextHref()
	{
		return nextHref;
	}

	public List<T> getItems()
	{
		return items;
	}

}
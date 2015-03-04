package org.molgenis.vkgl.api;

public class VkglResponseMetadata
{
	private String queryId;
	private String href;
	private int start;
	private int num;
	private long total;
	private String prevHref;
	private String nextHref;
	
	public String getHref()
	{
		return href;
	}
	public void setHref(String href)
	{
		this.href = href;
	}
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
	public long getTotal()
	{
		return total;
	}
	public void setTotal(long total)
	{
		this.total = total;
	}
	public String getPrevHref()
	{
		return prevHref;
	}
	public void setPrevHref(String prevHref)
	{
		this.prevHref = prevHref;
	}
	public String getNextHref()
	{
		return nextHref;
	}
	public void setNextHref(String nextHref)
	{
		this.nextHref = nextHref;
	}
	public String getQueryId()
	{
		return queryId;
	}
	public void setQueryId(String queryId)
	{
		this.queryId = queryId;
	}
}

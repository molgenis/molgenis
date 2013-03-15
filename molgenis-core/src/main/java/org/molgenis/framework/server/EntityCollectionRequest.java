package org.molgenis.framework.server;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class EntityCollectionRequest
{
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
}
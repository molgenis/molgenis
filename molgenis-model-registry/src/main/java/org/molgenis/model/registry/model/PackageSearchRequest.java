package org.molgenis.model.registry.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author sido
 */
public class PackageSearchRequest
{

	private String query;
	@Min(0)
	private Integer offset;
	@Min(0)
	@Max(100)
	private Integer num;

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public Integer getOffset()
	{
		return offset;
	}

	public void setOffset(Integer offset)
	{
		this.offset = offset;
	}

	public Integer getNum()
	{
		return num;
	}

	public void setNum(Integer num)
	{
		this.num = num;
	}
}

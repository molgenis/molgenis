package org.molgenis.data.rest;

public class VkglRequest
{
	private VkglQueryMetadata queryMetadata;
	private VkglQuery query;
	
	public VkglQueryMetadata getQueryMetadata()
	{
		return queryMetadata;
	}
	public void setQueryMetadata(VkglQueryMetadata queryMetadata)
	{
		this.queryMetadata = queryMetadata;
	}

	public VkglQuery getQuery()
	{
		return query;
	}
	public void setQuery(VkglQuery query)
	{
		this.query = query;
	}

}

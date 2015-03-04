package org.molgenis.vkgl.api;

public class VkglRequest
{
	private VkglQueryMetadata queryMetadata;
	private VkglQuery query;
	private String queryStatement;
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
	public String getQueryStatement()
	{
		return queryStatement;
	}
	public void setQueryStatement(String queryStatement)
	{
		this.queryStatement = queryStatement;
	}

}

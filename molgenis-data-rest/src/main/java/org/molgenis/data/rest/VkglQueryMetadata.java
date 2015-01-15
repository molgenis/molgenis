package org.molgenis.data.rest;

public class VkglQueryMetadata
{
	private String queryId;
	private String queryType;
	private String label;
	private String queryResultFormat;
	private VkglSubmitter submitter;
	
	public String getQueryType()
	{
		return queryType;
	}
	public void setQueryType(String queryType)
	{
		this.queryType = queryType;
	}
	public String getLabel()
	{
		return label;
	}
	public void setLabel(String label)
	{
		this.label = label;
	}
	public String getQueryResultFormat()
	{
		return queryResultFormat;
	}
	public void setQueryResultFormat(String queryResultFormat)
	{
		this.queryResultFormat = queryResultFormat;
	}
	public String getQueryId()
	{
		return queryId;
	}
	public void setQueryId(String queryId)
	{
		this.queryId = queryId;
	}
	public VkglSubmitter getSubmitter()
	{
		return submitter;
	}
	public void setSubmitter(VkglSubmitter submitter)
	{
		this.submitter = submitter;
	}
}

package org.molgenis.dataexplorer.controller;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.molgenis.data.QueryRule;

public class AggregateRequest
{
	@NotNull
	private String attributeUri;

	private List<QueryRule> q;

	public String getAttributeUri()
	{
		return attributeUri;
	}

	public void setAttributeUri(String attributeUri)
	{
		this.attributeUri = attributeUri;
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

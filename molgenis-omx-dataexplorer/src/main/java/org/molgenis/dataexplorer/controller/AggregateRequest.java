package org.molgenis.dataexplorer.controller;

import javax.validation.constraints.NotNull;

public class AggregateRequest
{
	@NotNull
	private String attributeUri;

	public String getAttributeUri()
	{
		return attributeUri;
	}

	public void setAttributeUri(String attributeUri)
	{
		this.attributeUri = attributeUri;
	}
}

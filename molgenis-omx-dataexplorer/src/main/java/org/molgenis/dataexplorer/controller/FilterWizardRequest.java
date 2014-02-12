package org.molgenis.dataexplorer.controller;

import javax.validation.constraints.NotNull;

public class FilterWizardRequest
{
	@NotNull
	private String entityUri;

	public String getEntityUri()
	{
		return entityUri;
	}

	public void setEntityUri(String entityUri)
	{
		this.entityUri = entityUri;
	}
}

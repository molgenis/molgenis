package org.molgenis.dataexplorer.controller;

import javax.validation.constraints.NotNull;

public class FilterWizardRequest
{

	private final String dataSetIdentifier;
	private final String dataSetName;
	private final String dataSetId;

	@NotNull
	public FilterWizardRequest(String dataSetIdentifier, String dataSetName, String dataSetId)
	{
		this.dataSetIdentifier = dataSetIdentifier;
		this.dataSetName = dataSetIdentifier;
		this.dataSetId = dataSetId;
	}

	public String getDataSetId()
	{
		return dataSetId;
	}

	public String getDataSetIdentifier()
	{
		return dataSetIdentifier;
	}

	public String getDataSetName()
	{
		return dataSetName;
	}

}

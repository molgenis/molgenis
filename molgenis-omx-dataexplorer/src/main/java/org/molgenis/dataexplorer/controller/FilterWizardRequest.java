package org.molgenis.dataexplorer.controller;

public class FilterWizardRequest
{

	private final String datasetIdentifier;
	private final String datasetName;
	private final String datasetId;

	public FilterWizardRequest(String datasetIdentifier, String datasetName, String datasetId)
	{
		this.datasetIdentifier = datasetIdentifier;
		this.datasetName = datasetIdentifier;
		this.datasetId = datasetId;
	}

	public String getDatasetId()
	{
		return datasetId;
	}

	public String getDatasetIdentifier()
	{
		return datasetIdentifier;
	}

	public String getDatasetName()
	{
		return datasetName;
	}

}

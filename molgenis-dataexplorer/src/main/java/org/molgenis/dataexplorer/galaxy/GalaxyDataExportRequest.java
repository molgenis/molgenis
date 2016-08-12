package org.molgenis.dataexplorer.galaxy;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.molgenis.dataexplorer.controller.DataRequest;

import javax.validation.constraints.NotNull;

public class GalaxyDataExportRequest
{
	@NotNull
	@URL
	private String galaxyUrl;

	@NotNull
	@Length(min = 32, max = 32)
	private String galaxyApiKey;

	@NotNull
	private DataRequest dataRequest;

	public String getGalaxyUrl()
	{
		return galaxyUrl;
	}

	public void setGalaxyUrl(String galaxyUrl)
	{
		this.galaxyUrl = galaxyUrl;
	}

	public String getGalaxyApiKey()
	{
		return galaxyApiKey;
	}

	public void setGalaxyApiKey(String galaxyApiKey)
	{
		this.galaxyApiKey = galaxyApiKey;
	}

	public DataRequest getDataRequest()
	{
		return dataRequest;
	}

	public void setDataRequest(DataRequest dataRequest)
	{
		this.dataRequest = dataRequest;
	}
}

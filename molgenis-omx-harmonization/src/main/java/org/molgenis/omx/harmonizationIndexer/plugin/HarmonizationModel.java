package org.molgenis.omx.harmonizationIndexer.plugin;

import java.io.Serializable;

public class HarmonizationModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String ontologyUri = null;

	private String errorMessage = null;

	public String getOntologyUri()
	{
		return ontologyUri;
	}

	public void setOntologyUri(String ontologyUri)
	{
		this.ontologyUri = ontologyUri;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage()
	{
		return this.errorMessage;
	}
}

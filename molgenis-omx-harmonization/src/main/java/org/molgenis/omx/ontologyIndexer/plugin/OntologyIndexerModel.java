package org.molgenis.omx.ontologyIndexer.plugin;

import java.io.Serializable;

public class OntologyIndexerModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String ontologyUri = null;

	private boolean correctOntology = false;

	private boolean startProcess = false;

	public String getOntologyUri()
	{
		return ontologyUri;
	}

	public void setOntologyUri(String ontologyUri)
	{
		this.ontologyUri = ontologyUri;
	}

	public void setCorrectOntology(boolean correctOntology)
	{
		this.correctOntology = correctOntology;
	}

	public boolean isCorrectOntology()
	{
		return this.correctOntology;
	}

	public boolean isStartProcess()
	{
		return startProcess;
	}

	public void setStartProcess(boolean startProcess)
	{
		this.startProcess = startProcess;
	}
}

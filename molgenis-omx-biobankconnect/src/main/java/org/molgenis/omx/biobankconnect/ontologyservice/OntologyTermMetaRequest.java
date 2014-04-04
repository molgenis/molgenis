package org.molgenis.omx.biobankconnect.ontologyservice;

public class OntologyTermMetaRequest
{
	private final String nodePath;
	private final String ontologyUrl;
	private final String ontologyTermUrl;

	public OntologyTermMetaRequest(String nodePath, String ontologyUrl, String ontologyTermUrl)
	{
		this.nodePath = nodePath;
		this.ontologyUrl = ontologyUrl;
		this.ontologyTermUrl = ontologyTermUrl;
	}

	public String getNodePath()
	{
		return nodePath;
	}

	public String getOntologyUrl()
	{
		return ontologyUrl;
	}

	public String getOntologyTermUrl()
	{
		return ontologyTermUrl;
	}
}
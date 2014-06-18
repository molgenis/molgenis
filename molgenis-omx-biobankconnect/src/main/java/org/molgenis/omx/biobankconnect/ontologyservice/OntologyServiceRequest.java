package org.molgenis.omx.biobankconnect.ontologyservice;


public class OntologyServiceRequest
{
	private final String ontologyUrl;
	private final String queryField;
	private final String queryString;

	public OntologyServiceRequest(String ontologyTermUrl, String queryField, String queryString)
	{
		this.ontologyUrl = ontologyTermUrl;
		this.queryField = queryField;
		this.queryString = queryString;
	}

	public String getOntologyUrl()
	{
		return ontologyUrl;
	}

	public String getQueryString()
	{
		return queryString;
	}

	public String getQueryField()
	{
		return queryField;
	}
}
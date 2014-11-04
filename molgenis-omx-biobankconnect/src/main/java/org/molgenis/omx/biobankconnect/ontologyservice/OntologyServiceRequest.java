package org.molgenis.omx.biobankconnect.ontologyservice;


public class OntologyServiceRequest
{
	private final String ontologyIri;
	private final String queryField;
	private final String queryString;

	public OntologyServiceRequest(String ontologyIri, String queryField, String queryString)
	{
		this.ontologyIri = ontologyIri;
		this.queryField = queryField;
		this.queryString = queryString;
	}

	public String getOntologyIri()
	{
		return ontologyIri;
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
package org.molgenis.data.semantic;

public enum Relation
{
	instanceOf("http://molgenis.org/biobankconnect/instanceOf");

	private String iri;

	private Relation(String iri)
	{
		this.iri = iri;
	}

	public String getIRI()
	{
		return iri;
	}

	public String getLabel()
	{
		return toString();
	}

}

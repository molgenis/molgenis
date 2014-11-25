package org.molgenis.data.semantic;

public enum Relation
{
	instanceOf("http://molgenis.org/biobankconnect/instanceOf"), link("http://molgenis.org/biobankconnect/link"), homepage(
			"http://xmlns.com/foaf/0.1/homepage"), isDefinedBy("http://www.w3.org/2000/01/rdf-schema#isDefinedBy"), seeAlso(
			"http://www.w3.org/2000/01/rdf-schema#seeAlso");

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

	public static Relation forIRI(String string)
	{
		for (Relation relation : values())
		{
			if (relation.getIRI().equals(string))
			{
				return relation;
			}
		}
		return null;
	}

}

package org.molgenis.data.semantic;

public enum Relation
{
	instanceOf("http://molgenis.org/biobankconnect/instanceOf"), link("http://molgenis.org/biobankconnect/link");

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
